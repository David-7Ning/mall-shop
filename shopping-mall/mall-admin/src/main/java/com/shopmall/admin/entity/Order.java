package com.shopmall.admin.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类（与 mall-order 保持一致）
 */
@Data
public class Order {

    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 订单状态：0待付款 1已付款 2已发货 3已完成 4已取消 */
    private Integer status;

    /** 收货地址 */
    private String address;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}