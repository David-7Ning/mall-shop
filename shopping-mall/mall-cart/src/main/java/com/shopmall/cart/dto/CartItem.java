package com.shopmall.cart.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车项：一个商品在购物车中的记录
 * 存储在Redis中，key为 cart:userId，value为Hash
 */
@Data
public class CartItem implements Serializable {

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 商品价格 */
    private BigDecimal price;

    /** 商品图片 */
    private String imageUrl;

    /** 购买数量 */
    private Integer quantity;

    /** 是否选中 */
    private Boolean checked = true;
}