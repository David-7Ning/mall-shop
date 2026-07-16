package com.shopmall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopmall.admin.entity.Order;
import com.shopmall.admin.feign.OrderFeignClient;
import com.shopmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理 - 订单管理
 */
@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    /** 分页查询订单列表 */
    @GetMapping("/list")
    public Result<Page<Order>> listAdminPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        return orderFeignClient.listAdminPage(pageNum, pageSize, orderNo, userId, status);
    }

    /** 发货 */
    @PutMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id) {
        return orderFeignClient.shipOrder(id);
    }
}