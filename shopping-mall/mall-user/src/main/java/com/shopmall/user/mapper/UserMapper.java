package com.shopmall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopmall.user.entity.User;

/**
 * 用户Mapper接口
 * 继承BaseMapper后，自动拥有CRUD方法，不用写SQL
 * 如：insert、deleteById、updateById、selectById、selectList等
 */
public interface UserMapper extends BaseMapper<User> {
}