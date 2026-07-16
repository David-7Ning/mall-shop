package com.shopmall.order.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建订单请求参数
 */
@Data
public class CreateOrderRequest {

    /** 用户ID */
    private Long userId;

    /** 收货地址 */
    private String address;

    /** 订单商品列表 */
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        /** 商品ID */
        private Long productId;
        /** 商品名称 */
        private String productName;
        /** 商品单价 */
        private java.math.BigDecimal price;
        /** 购买数量 */
        private Integer quantity;
    }
}