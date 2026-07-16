package com.shopmall.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopmall.common.result.Result;
import com.shopmall.order.dto.CreateOrderRequest;
import com.shopmall.order.dto.DashboardStats;
import com.shopmall.order.dto.OrderDetailResponse;
import com.shopmall.order.entity.Order;
import com.shopmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /** 创建订单：POST /orders */
    @PostMapping
    public Result<String> create(@RequestBody CreateOrderRequest request) {
        String orderNo = orderService.createOrder(request);
        return Result.success(orderNo);
    }

    /** 查询用户订单列表：GET /orders?userId=1&status=0 */
    @GetMapping
    public Result<List<Order>> list(@RequestParam Long userId, @RequestParam(required = false) Integer status) {
        return Result.success(orderService.listByUserId(userId, status));
    }

    /** 查询订单详情（包含商品明细）：GET /orders/{id} */
    @GetMapping("/{id}")
    public Result<OrderDetailResponse> detail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    /** 取消订单：PUT /orders/{id}/cancel */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success();
    }

    /** 支付订单：PUT /orders/{id}/pay */
    @PutMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        orderService.payOrder(id);
        return Result.success();
    }

    // ========== 后台管理接口 ==========

    /** 分页查询订单列表：GET /order/admin/list */
    @GetMapping("/admin/list")
    public Result<Page<Order>> listAdminPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        return Result.success(orderService.listAdminPage(pageNum, pageSize, orderNo, userId, status));
    }

    /** 发货：PUT /order/admin/{id}/ship */
    @PutMapping("/admin/{id}/ship")
    public Result<Void> ship(@PathVariable Long id) {
        orderService.shipOrder(id);
        return Result.success();
    }

    /** 获取统计数据：GET /order/admin/stats */
    @GetMapping("/admin/stats")
    public Result<DashboardStats> getStats() {
        return Result.success(orderService.getDashboardStats());
    }
}