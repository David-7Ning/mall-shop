package com.shopmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shopmall.product.entity.Product;

import java.util.List;

public interface ProductService extends IService<Product> {

    /** 查询上架商品列表 */
    List<Product> listOnSale();

    /** 根据分类查询商品 */
    List<Product> listByCategory(Long categoryId);

    /** 搜索商品（按关键词） */
    List<Product> searchProducts(String keyword);

    /** 扣减库存（下单时调用） */
    boolean reduceStock(Long productId, Integer quantity);

    // ========== 后台管理接口 ==========

    /** 分页查询商品列表（后台管理用） */
    com.baomidou.mybatisplus.core.metadata.IPage<Product> listAdminPage(Integer pageNum, Integer pageSize, String name, Long categoryId, Integer status);

    /** 新增商品（后台管理用） */
    void addProduct(Product product);

    /** 编辑商品（后台管理用） */
    void updateProduct(Product product);

    /** 删除商品（后台管理用） */
    void deleteProduct(Long id);

    /** 切换商品上下架状态（后台管理用） */
    void updateStatus(Long id, Integer status);

    /** 查询所有分类列表 */
    List<com.shopmall.product.entity.Category> listCategories();
}