package com.shopmall.cart.controller;

import com.shopmall.cart.dto.CartItem;
import com.shopmall.cart.service.CartService;
import com.shopmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /** 添加商品到购物车：POST /cart/items */
    @PostMapping("/items")
    public Result<Void> add(@RequestBody CartItem cartItem) {
        cartService.addItem(cartItem.getUserId(), cartItem);
        return Result.success();
    }

    /** 修改商品数量：PUT /cart/items/{productId} */
    @PutMapping("/items/{productId}")
    public Result<Void> update(@PathVariable Long productId, @RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        cartService.updateQuantity(userId, productId, quantity);
        return Result.success();
    }

    /** 删除购物车商品：DELETE /cart/items/{productId} */
    @DeleteMapping("/items/{productId}")
    public Result<Void> remove(@PathVariable Long productId, @RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        cartService.removeItem(userId, productId);
        return Result.success();
    }

    /** 查询购物车：GET /cart/items?userId=1 */
    @GetMapping("/items")
    public Result<List<CartItem>> list(@RequestParam Long userId) {
        return Result.success(cartService.listItems(userId));
    }

    /** 清空购物车：DELETE /cart/items?userId=1 */
    @DeleteMapping("/items")
    public Result<Void> clear(@RequestParam Long userId) {
        cartService.clearCart(userId);
        return Result.success();
    }
}