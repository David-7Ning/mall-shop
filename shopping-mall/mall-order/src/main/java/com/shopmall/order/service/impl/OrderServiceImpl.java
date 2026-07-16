package com.shopmall.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopmall.common.exception.BusinessException;
import com.shopmall.common.result.Result;
import com.shopmall.common.result.ResultCode;
import com.shopmall.order.dto.CreateOrderRequest;
import com.shopmall.order.dto.DashboardStats;
import com.shopmall.order.dto.OrderDetailResponse;
import com.shopmall.order.entity.Order;
import com.shopmall.order.entity.OrderItem;
import com.shopmall.order.feign.ProductFeignClient;
import com.shopmall.order.mapper.OrderItemMapper;
import com.shopmall.order.mapper.OrderMapper;
import com.shopmall.order.mq.OrderDelayProducer;
import com.shopmall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private OrderDelayProducer orderDelayProducer;

    /** 订单列表缓存 Key 前缀 */
    private static final String ORDER_LIST_KEY_PREFIX = "order:list:";
    /** 订单详情缓存 Key 前缀 */
    private static final String ORDER_DETAIL_KEY_PREFIX = "order:detail:";
    /** 缓存过期时间：10 分钟 */
    private static final long CACHE_EXPIRE_MINUTES = 10;

    @Override
    @Transactional  // 事务：创建订单和扣减库存必须在同一个事务中
    public String createOrder(CreateOrderRequest request) {
        // 1. 生成订单号（使用雪花算法生成唯一ID）
        String orderNo = IdUtil.getSnowflakeNextIdStr();

        // 2. 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateOrderRequest.OrderItemDTO item : request.getItems()) {
            BigDecimal subtotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }

        // 3. 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setTotalAmount(totalAmount);
        order.setStatus(0);  // 0=待付款
        order.setAddress(request.getAddress());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        save(order);

        // 4. 创建订单明细
        for (CreateOrderRequest.OrderItemDTO item : request.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName());
            orderItem.setPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
            orderItemMapper.insert(orderItem);
        }

        // 5. 调用商品服务扣减库存
        for (CreateOrderRequest.OrderItemDTO item : request.getItems()) {
            Result<Void> result = productFeignClient.reduceStock(item.getProductId(), item.getQuantity());
            if (result == null || result.getCode() != 200) {
                throw new BusinessException("扣减库存失败：" + (result == null ? "服务不可用" : result.getMessage()));
            }
        }

        // 6. 发送延迟消息（30 分钟后自动取消）
        orderDelayProducer.sendOrderDelayMessage(order.getId());

        // 7. 删除该用户的订单列表缓存（主动删除策略）
        evictUserOrderCache(request.getUserId());

        return orderNo;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Order> listByUserId(Long userId, Integer status) {
        // 构建缓存 Key
        String cacheKey = ORDER_LIST_KEY_PREFIX + userId + ":" + (status == null ? "all" : status);

        // 1. 先查 Redis 缓存
        List<Order> cachedOrders = (List<Order>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedOrders != null && !cachedOrders.isEmpty()) {
            return cachedOrders;
        }

        // 2. 缓存未命中，查数据库
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        List<Order> orders = list(wrapper);

        // 3. 写入缓存
        if (orders != null && !orders.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, orders, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }

        return orders;
    }

    @Override
    public OrderDetailResponse getOrderDetail(Long orderId) {
        // 构建缓存 Key
        String cacheKey = ORDER_DETAIL_KEY_PREFIX + orderId;

        // 1. 先查 Redis 缓存
        OrderDetailResponse cachedDetail = (OrderDetailResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cachedDetail != null) {
            return cachedDetail;
        }

        // 2. 缓存未命中，查数据库
        // 2.1 查询订单基本信息
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 2.2 构建响应对象
        OrderDetailResponse response = new OrderDetailResponse();
        BeanUtils.copyProperties(order, response);

        // 2.3 查询订单商品明细
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        response.setItems(items);

        // 3. 写入缓存
        redisTemplate.opsForValue().set(cacheKey, response, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return response;
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只能取消待付款的订单");
        }
        order.setStatus(4);  // 4=已取消
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        // 主动删除缓存
        evictOrderCache(orderId, order.getUserId());
    }

    @Override
    @Transactional
    public void payOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只能支付待付款的订单");
        }
        order.setStatus(1);  // 1=已付款
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        // 主动删除缓存
        evictOrderCache(orderId, order.getUserId());
    }

    /**
     * 清除指定用户的订单列表缓存
     */
    private void evictUserOrderCache(Long userId) {
        redisTemplate.delete(ORDER_LIST_KEY_PREFIX + userId + ":all");
        redisTemplate.delete(ORDER_LIST_KEY_PREFIX + userId + ":0");
        redisTemplate.delete(ORDER_LIST_KEY_PREFIX + userId + ":1");
        redisTemplate.delete(ORDER_LIST_KEY_PREFIX + userId + ":2");
        redisTemplate.delete(ORDER_LIST_KEY_PREFIX + userId + ":3");
        redisTemplate.delete(ORDER_LIST_KEY_PREFIX + userId + ":4");
    }

    /**
     * 清除指定订单的缓存（详情 + 用户列表）
     */
    private void evictOrderCache(Long orderId, Long userId) {
        // 删除订单详情缓存
        redisTemplate.delete(ORDER_DETAIL_KEY_PREFIX + orderId);
        // 删除用户订单列表缓存
        evictUserOrderCache(userId);
    }

    @Override
    @Transactional
    public void cancelTimeoutOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            log.warn("订单不存在，orderId: " + orderId);
            return;
        }
        // 只有待付款的订单才能被超时取消
        if (order.getStatus() != 0) {
            log.info("订单状态不是待付款，跳过取消，orderId: " + orderId + ", status: " + order.getStatus());
            return;
        }
        order.setStatus(4);  // 4=已取消
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        log.info("超时订单已自动取消，orderId: " + orderId);

        // 主动删除缓存
        evictOrderCache(orderId, order.getUserId());
    }

    @Override
    public Page<Order> listAdminPage(Integer pageNum, Integer pageSize, String orderNo, Long userId, Integer status) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        
        // 按订单号模糊查询
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(Order::getOrderNo, orderNo);
        }
        // 按用户ID查询
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        // 按状态查询
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        
        wrapper.orderByDesc(Order::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional
    public void shipOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("只能对已付款的订单进行发货");
        }
        order.setStatus(2);  // 2=已发货
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        // 主动删除缓存
        evictOrderCache(orderId, order.getUserId());
    }

    @Override
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        // 1. 总销售额（已付款 + 已发货 + 已完成的订单）
        LambdaQueryWrapper<Order> salesWrapper = new LambdaQueryWrapper<>();
        salesWrapper.in(Order::getStatus, 1, 2, 3);
        List<Order> paidOrders = list(salesWrapper);
        BigDecimal totalSales = paidOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalSales(totalSales);

        // 2. 总订单数
        stats.setTotalOrders(count());

        // 3. 今日订单数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LambdaQueryWrapper<Order> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Order::getCreateTime, todayStart);
        stats.setTodayOrders(count(todayWrapper));

        // 4. 热门商品排行（按销量排序，取前10）
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.select(OrderItem::getProductId, OrderItem::getProductName, OrderItem::getQuantity);
        List<OrderItem> allItems = orderItemMapper.selectList(itemWrapper);

        // 按商品ID分组统计销量
        Map<Long, DashboardStats.ProductSales> productSalesMap = new LinkedHashMap<>();
        for (OrderItem item : allItems) {
            DashboardStats.ProductSales ps = productSalesMap.computeIfAbsent(item.getProductId(),
                    k -> {
                        DashboardStats.ProductSales newPs = new DashboardStats.ProductSales();
                        newPs.setProductId(k);
                        newPs.setProductName(item.getProductName());
                        newPs.setSalesCount(0L);
                        return newPs;
                    });
            ps.setSalesCount(ps.getSalesCount() + item.getQuantity());
        }

        // 按销量排序，取前10
        List<DashboardStats.ProductSales> topProducts = productSalesMap.values().stream()
                .sorted((a, b) -> Long.compare(b.getSalesCount(), a.getSalesCount()))
                .limit(10)
                .collect(Collectors.toList());
        stats.setTopProducts(topProducts);

        return stats;
    }
}