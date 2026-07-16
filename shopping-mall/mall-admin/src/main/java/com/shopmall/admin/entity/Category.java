package com.shopmall.admin.entity;

import lombok.Data;

/**
 * 商品分类实体类（与 mall-product 保持一致）
 */
@Data
public class Category {

    private Long id;

    /** 分类名称 */
    private String name;

    /** 父分类ID（0表示一级分类） */
    private Long parentId;

    /** 排序号 */
    private Integer sort;
}