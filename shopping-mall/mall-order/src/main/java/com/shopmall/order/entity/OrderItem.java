package com.shopmall.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细：一个订单包含多个商品
 */
@Data
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 商品ID */
    private Long productId;

    /** 商品名称（冗余存储，防止商品改名后订单数据不一致） */
    private String productName;

    /** 商品单价（下单时的价格，不是当前价格） */
    private BigDecimal price;

    /** 购买数量 */
    private Integer quantity;

    /** 小计金额 = price × quantity */
    private BigDecimal subtotal;
}