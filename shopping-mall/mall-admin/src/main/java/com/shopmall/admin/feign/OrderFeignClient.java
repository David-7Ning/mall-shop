package com.shopmall.admin.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopmall.admin.entity.Order;
import com.shopmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单服务 Feign 客户端
 */
@FeignClient(name = "mall-order", path = "/order")
public interface OrderFeignClient {

    /** 分页查询订单列表 */
    @GetMapping("/admin/list")
    Result<Page<Order>> listAdminPage(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "status", required = false) Integer status);

    /** 发货 */
    @PutMapping("/admin/{id}/ship")
    Result<Void> shipOrder(@PathVariable("id") Long id);

    /** 获取统计数据 */
    @GetMapping("/admin/stats")
    Result<Map<String, Object>> getStats();
}