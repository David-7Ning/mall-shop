package com.shopmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopmall.common.exception.BusinessException;
import com.shopmall.common.result.ResultCode;
import com.shopmall.product.entity.Category;
import com.shopmall.product.entity.Product;
import com.shopmall.product.mapper.CategoryMapper;
import com.shopmall.product.mapper.ProductMapper;
import com.shopmall.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CategoryMapper categoryMapper;

    private static final String PRODUCT_LIST_KEY = "product:list:on_sale";

    @Override
    public List<Product> listOnSale() {
        // 1. 先查 Redis 缓存
        List<Product> cachedProducts = (List<Product>) redisTemplate.opsForValue().get(PRODUCT_LIST_KEY);
        if (cachedProducts != null && !cachedProducts.isEmpty()) {
            return cachedProducts;
        }

        // 2. 缓存未命中，查数据库
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .orderByDesc(Product::getCreateTime);
        List<Product> products = list(wrapper);

        // 3. 写入缓存，设置过期时间 30 分钟
        if (products != null && !products.isEmpty()) {
            redisTemplate.opsForValue().set(PRODUCT_LIST_KEY, products, 30, TimeUnit.MINUTES);
        }

        return products;
    }

    @Override
    public List<Product> listByCategory(Long categoryId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, categoryId)
                .eq(Product::getStatus, 1);
        return list(wrapper);
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .and(w -> w.like(Product::getName, keyword)
                        .or()
                        .like(Product::getDescription, keyword))
                .orderByDesc(Product::getCreateTime);
        return list(wrapper);
    }

    @Override
    public boolean reduceStock(Long productId, Integer quantity) {
        // 使用SQL条件：stock >= quantity，防止超卖
        LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Product::getId, productId)
                .ge(Product::getStock, quantity)
                .setSql("stock = stock - " + quantity);
        boolean success = update(wrapper);
        
        // 库存更新成功后，主动删除缓存，确保多实例环境下数据一致性
        if (success) {
            evictProductListCache();
        }
        
        if (!success) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }
        return true;
    }

    /**
     * 清除商品列表缓存
     * 在商品增删改、库存变更时调用
     */
    private void evictProductListCache() {
        redisTemplate.delete(PRODUCT_LIST_KEY);
    }

    // ========== 后台管理接口实现 ==========

    @Override
    public IPage<Product> listAdminPage(Integer pageNum, Integer pageSize, String name, Long categoryId, Integer status) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        
        // 条件筛选
        if (StringUtils.hasText(name)) {
            wrapper.like(Product::getName, name);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        
        return page(page, wrapper);
    }

    @Override
    public void addProduct(Product product) {
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        if (product.getStatus() == null) {
            product.setStatus(0); // 默认下架
        }
        save(product);
        evictProductListCache();
    }

    @Override
    public void updateProduct(Product product) {
        Product existProduct = getById(product.getId());
        if (existProduct == null) {
            throw new BusinessException("商品不存在");
        }
        product.setUpdateTime(LocalDateTime.now());
        updateById(product);
        evictProductListCache();
    }

    @Override
    public void deleteProduct(Long id) {
        Product existProduct = getById(id);
        if (existProduct == null) {
            throw new BusinessException("商品不存在");
        }
        removeById(id);
        evictProductListCache();
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product existProduct = getById(id);
        if (existProduct == null) {
            throw new BusinessException("商品不存在");
        }
        LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Product::getId, id)
                .set(Product::getStatus, status)
                .set(Product::getUpdateTime, LocalDateTime.now());
        update(wrapper);
        evictProductListCache();
    }

    @Override
    public List<Category> listCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort, Category::getId);
        return categoryMapper.selectList(wrapper);
    }
}