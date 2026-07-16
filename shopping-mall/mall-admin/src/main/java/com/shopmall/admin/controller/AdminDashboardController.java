package com.shopmall.admin.controller;

import com.shopmall.admin.feign.OrderFeignClient;
import com.shopmall.admin.feign.UserFeignClient;
import com.shopmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台管理 - 数据统计仪表盘
 */
@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;

    /** 获取仪表盘统计数据 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> result = new HashMap<>();

        // 1. 获取订单统计数据
        Result<Map<String, Object>> orderStatsResult = orderFeignClient.getStats();
        if (orderStatsResult != null && orderStatsResult.getCode() == 200) {
            Map<String, Object> orderStats = orderStatsResult.getData();
            result.put("totalSales", orderStats.get("totalSales"));
            result.put("totalOrders", orderStats.get("totalOrders"));
            result.put("todayOrders", orderStats.get("todayOrders"));
            result.put("topProducts", orderStats.get("topProducts"));
        }

        // 2. 获取用户总数
        Result<Long> userCountResult = userFeignClient.getUserCount();
        if (userCountResult != null && userCountResult.getCode() == 200) {
            result.put("totalUsers", userCountResult.getData());
        }

        return Result.success(result);
    }
}