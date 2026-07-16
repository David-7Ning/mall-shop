package com.shopmall.admin.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类（后台管理用，与 mall-product 保持一致）
 */
@Data
public class Product {

    private Long id;

    /** 商品名称 */
    private String name;

    /** 商品描述 */
    private String description;

    /** 商品价格 */
    private BigDecimal price;

    /** 库存数量 */
    private Integer stock;

    /** 商品分类ID */
    private Long categoryId;

    /** 商品图片URL */
    private String imageUrl;

    /** 上架状态：0下架 1上架 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}