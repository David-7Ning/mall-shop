package com.shopmall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shopmall.common.result.Result;
import com.shopmall.product.entity.Category;
import com.shopmall.product.entity.Product;
import com.shopmall.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /** 查询上架商品列表：GET /product/list?categoryId=xxx */
    @GetMapping("/list")
    public Result<List<Product>> list(@RequestParam(required = false) Long categoryId) {
        if (categoryId != null) {
            return Result.success(productService.listByCategory(categoryId));
        }
        return Result.success(productService.listOnSale());
    }

    /** 根据分类查询商品：GET /product/category/{categoryId} */
    @GetMapping("/category/{categoryId}")
    public Result<List<Product>> listByCategory(@PathVariable Long categoryId) {
        return Result.success(productService.listByCategory(categoryId));
    }

    /** 搜索商品：GET /product/search?keyword=xxx */
    @GetMapping("/search")
    public Result<List<Product>> search(@RequestParam String keyword) {
        return Result.success(productService.searchProducts(keyword));
    }

    /** 查询商品详情：GET /product/{id} */
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    /** 扣减库存：PUT /product/{id}/stock */
    @PutMapping("/{id}/stock")
    public Result<Void> reduceStock(@PathVariable Long id,
                                    @RequestParam Integer quantity) {
        productService.reduceStock(id, quantity);
        return Result.success();
    }

    // ========== 后台管理接口 ==========

    /** 分页查询商品列表：GET /product/admin/list */
    @GetMapping("/admin/list")
    public Result<IPage<Product>> listAdminPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return Result.success(productService.listAdminPage(pageNum, pageSize, name, categoryId, status));
    }

    /** 新增商品：POST /product/admin */
    @PostMapping("/admin")
    public Result<Void> addProduct(@RequestBody Product product) {
        productService.addProduct(product);
        return Result.success();
    }

    /** 编辑商品：PUT /product/admin */
    @PutMapping("/admin")
    public Result<Void> updateProduct(@RequestBody Product product) {
        productService.updateProduct(product);
        return Result.success();
    }

    /** 删除商品：DELETE /product/admin/{id} */
    @DeleteMapping("/admin/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }

    /** 切换上下架状态：PUT /product/admin/{id}/status */
    @PutMapping("/admin/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return Result.success();
    }

    /** 查询分类列表：GET /product/admin/categories */
    @GetMapping("/admin/categories")
    public Result<List<Category>> listCategories() {
        return Result.success(productService.listCategories());
    }
}