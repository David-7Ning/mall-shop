package com.shopmall.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shopmall.order.dto.CreateOrderRequest;
import com.shopmall.order.dto.OrderDetailResponse;
import com.shopmall.order.entity.Order;

import java.util.List;

public interface OrderService extends IService<Order> {

    /** 创建订单 */
    String createOrder(CreateOrderRequest request);

    /** 查询用户的订单列表 */
    List<Order> listByUserId(Long userId, Integer status);

    /** 查询订单详情（包含商品明细） */
    OrderDetailResponse getOrderDetail(Long orderId);

    /** 取消订单 */
    void cancelOrder(Long orderId);

    /** 支付订单 */
    void payOrder(Long orderId);

    /** 取消超时未支付的订单（由 MQ 消费者调用） */
    void cancelTimeoutOrder(Long orderId);

    /** 后台管理：分页查询订单列表 */
    Page<Order> listAdminPage(Integer pageNum, Integer pageSize, String orderNo, Long userId, Integer status);

    /** 后台管理：发货 */
    void shipOrder(Long orderId);

    /** 后台管理：获取统计数据 */
    com.shopmall.order.dto.DashboardStats getDashboardStats();
}