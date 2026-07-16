package com.shopmall.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 统计数据 DTO
 */
@Data
public class DashboardStats {

    /** 总销售额 */
    private BigDecimal totalSales;

    /** 总订单数 */
    private Long totalOrders;

    /** 今日订单数 */
    private Long todayOrders;

    /** 总用户数 */
    private Long totalUsers;

    /** 热门商品排行（商品ID -> 销量） */
    private java.util.List<ProductSales> topProducts;

    @Data
    public static class ProductSales {
        private Long productId;
        private String productName;
        private Long salesCount;
    }
}