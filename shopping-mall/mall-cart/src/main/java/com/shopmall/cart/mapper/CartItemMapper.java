package com.shopmall.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopmall.cart.entity.CartItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车 Mapper 接口
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItemEntity> {
}