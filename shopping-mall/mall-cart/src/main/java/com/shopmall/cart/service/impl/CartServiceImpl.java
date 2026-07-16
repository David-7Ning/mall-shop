package com.shopmall.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shopmall.cart.dto.CartItem;
import com.shopmall.cart.entity.CartItemEntity;
import com.shopmall.cart.mapper.CartItemMapper;
import com.shopmall.cart.service.CartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 购物车Service实现类
 * 采用 Redis + MySQL 双写架构：
 * - Redis：提供高速读写（用户体验）
 * - MySQL：数据持久化（换设备登录不丢失）
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CartItemMapper cartItemMapper;

    /** 拼接购物车的Redis key */
    private String getCartKey(Long userId) {
        return "cart:" + userId;
    }

    /** DTO 转 Entity */
    private CartItemEntity toEntity(CartItem dto) {
        CartItemEntity entity = new CartItemEntity();
        BeanUtils.copyProperties(dto, entity);
        entity.setChecked(dto.getChecked() ? 1 : 0);
        return entity;
    }

    /** Entity 转 DTO */
    private CartItem toDto(CartItemEntity entity) {
        CartItem dto = new CartItem();
        BeanUtils.copyProperties(entity, dto);
        dto.setChecked(entity.getChecked() == 1);
        return dto;
    }

    @Override
    public void addItem(Long userId, CartItem cartItem) {
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        String cartKey = getCartKey(userId);
        String productId = String.valueOf(cartItem.getProductId());

        // 1. 先写 Redis（保证用户体验）
        Object existing = hashOps.get(cartKey, productId);
        if (existing != null) {
            CartItem existingItem = (CartItem) existing;
            cartItem.setQuantity(cartItem.getQuantity() + existingItem.getQuantity());
        }
        hashOps.put(cartKey, productId, cartItem);

        // 2. 再写 MySQL（保证数据持久化）
        LambdaQueryWrapper<CartItemEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItemEntity::getUserId, userId)
               .eq(CartItemEntity::getProductId, cartItem.getProductId());
        CartItemEntity dbItem = cartItemMapper.selectOne(wrapper);

        if (dbItem != null) {
            // 商品已存在，更新数量
            dbItem.setQuantity(dbItem.getQuantity() + cartItem.getQuantity());
            cartItemMapper.updateById(dbItem);
        } else {
            // 商品不存在，新增
            cartItemMapper.insert(toEntity(cartItem));
        }
    }

    @Override
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        String cartKey = getCartKey(userId);
        String hashKey = String.valueOf(productId);

        // 1. 更新 Redis
        Object existing = hashOps.get(cartKey, hashKey);
        if (existing != null) {
            CartItem cartItem = (CartItem) existing;
            cartItem.setQuantity(quantity);
            hashOps.put(cartKey, hashKey, cartItem);
        }

        // 2. 更新 MySQL
        LambdaUpdateWrapper<CartItemEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CartItemEntity::getUserId, userId)
               .eq(CartItemEntity::getProductId, productId)
               .set(CartItemEntity::getQuantity, quantity);
        cartItemMapper.update(null, wrapper);
    }

    @Override
    public void removeItem(Long userId, Long productId) {
        // 1. 删除 Redis
        redisTemplate.opsForHash().delete(getCartKey(userId), String.valueOf(productId));

        // 2. 删除 MySQL
        LambdaQueryWrapper<CartItemEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItemEntity::getUserId, userId)
               .eq(CartItemEntity::getProductId, productId);
        cartItemMapper.delete(wrapper);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CartItem> listItems(Long userId) {
        // 优先从 Redis 读取
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(getCartKey(userId));
        if (entries != null && !entries.isEmpty()) {
            List<CartItem> items = new ArrayList<>();
            for (Object value : entries.values()) {
                items.add((CartItem) value);
            }
            return items;
        }

        // Redis 未命中，从 MySQL 读取并回写 Redis
        LambdaQueryWrapper<CartItemEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItemEntity::getUserId, userId);
        List<CartItemEntity> entities = cartItemMapper.selectList(wrapper);

        List<CartItem> items = new ArrayList<>();
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        for (CartItemEntity entity : entities) {
            CartItem dto = toDto(entity);
            items.add(dto);
            hashOps.put(getCartKey(userId), String.valueOf(entity.getProductId()), dto);
        }

        return items;
    }

    @Override
    public void clearCart(Long userId) {
        // 1. 清空 Redis
        redisTemplate.delete(getCartKey(userId));

        // 2. 清空 MySQL
        LambdaQueryWrapper<CartItemEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItemEntity::getUserId, userId);
        cartItemMapper.delete(wrapper);
    }
}