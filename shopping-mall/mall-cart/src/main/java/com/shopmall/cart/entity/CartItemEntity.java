package com.shopmall.cart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体类，对应数据库 cart_item 表
 */
@Data
@TableName("cart_item")
public class CartItemEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

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

    /** 是否选中：0否 1是 */
    private Integer checked;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}