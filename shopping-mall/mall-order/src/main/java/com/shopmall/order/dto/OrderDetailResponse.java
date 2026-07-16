package com.shopmall.order.dto;

import com.shopmall.order.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情响应（包含订单信息和商品明细）
 */
@Data
public class OrderDetailResponse {

    /** 订单ID */
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

    /** 订单商品明细列表 */
    private List<OrderItem> items;
}