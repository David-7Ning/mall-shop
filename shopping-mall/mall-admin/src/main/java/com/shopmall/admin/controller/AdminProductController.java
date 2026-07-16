package com.shopmall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopmall.admin.entity.Category;
import com.shopmall.admin.entity.Product;
import com.shopmall.admin.feign.ProductFeignClient;
import com.shopmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台商品管理 Controller
 */
@RestController
@RequestMapping("/admin/product")
public class AdminProductController {

    @Autowired
    private ProductFeignClient productFeignClient;

    /** 分页查询商品列表 */
    @GetMapping("/list")
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return productFeignClient.listAdminPage(pageNum, pageSize, name, categoryId, status);
    }

    /** 新增商品 */
    @PostMapping
    public Result<Void> add(@RequestBody Product product) {
        return productFeignClient.addProduct(product);
    }

    /** 编辑商品 */
    @PutMapping
    public Result<Void> update(@RequestBody Product product) {
        return productFeignClient.updateProduct(product);
    }

    /** 删除商品 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return productFeignClient.deleteProduct(id);
    }

    /** 切换上下架状态 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return productFeignClient.updateStatus(id, status);
    }

    /** 查询分类列表 */
    @GetMapping("/categories")
    public Result<List<Category>> listCategories() {
        return productFeignClient.listCategories();
    }
}