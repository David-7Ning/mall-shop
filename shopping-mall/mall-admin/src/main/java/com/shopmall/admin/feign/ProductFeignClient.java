package com.shopmall.admin.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopmall.admin.entity.Category;
import com.shopmall.admin.entity.Product;
import com.shopmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品服务 Feign 客户端
 */
@FeignClient(name = "mall-product")
public interface ProductFeignClient {

    /** 分页查询商品列表 */
    @GetMapping("/product/admin/list")
    Result<Page<Product>> listAdminPage(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "status", required = false) Integer status);

    /** 新增商品 */
    @PostMapping("/product/admin")
    Result<Void> addProduct(@RequestBody Product product);

    /** 编辑商品 */
    @PutMapping("/product/admin")
    Result<Void> updateProduct(@RequestBody Product product);

    /** 删除商品 */
    @DeleteMapping("/product/admin/{id}")
    Result<Void> deleteProduct(@PathVariable("id") Long id);

    /** 切换上下架状态 */
    @PutMapping("/product/admin/{id}/status")
    Result<Void> updateStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status);

    /** 查询分类列表 */
    @GetMapping("/product/admin/categories")
    Result<List<Category>> listCategories();
}