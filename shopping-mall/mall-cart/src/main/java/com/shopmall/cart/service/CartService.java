package com.shopmall.cart.service;

import com.shopmall.cart.dto.CartItem;

import java.util.List;

public interface CartService {

    /** 添加商品到购物车 */
    void addItem(Long userId, CartItem cartItem);

    /** 修改购物车商品数量 */
    void updateQuantity(Long userId, Long productId, Integer quantity);

    /** 删除购物车商品 */
    void removeItem(Long userId, Long productId);

    /** 查询购物车列表 */
    List<CartItem> listItems(Long userId);

    /** 清空购物车 */
    void clearCart(Long userId);
}