package com.shopmall.user.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopmall.common.exception.BusinessException;
import com.shopmall.common.result.ResultCode;
import com.shopmall.common.util.JwtUtil;
import com.shopmall.user.dto.LoginResponse;
import com.shopmall.user.entity.User;
import com.shopmall.user.mapper.UserMapper;
import com.shopmall.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户Service实现类
 * 继承ServiceImpl<Mapper, Entity>，自动实现IService中的方法
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public void register(String username, String password) {
        // 1. 检查用户名是否已存在
        User existUser = getByUsername(username);
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        // 2. 创建用户，密码用BCrypt加密
        User user = new User();
        user.setUsername(username);
        user.setPassword(BCrypt.hashpw(password));
        user.setNickname(username);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        // 3. 保存到数据库
        save(user);
    }

    @Override
    public LoginResponse login(String username, String password) {
        // 1. 根据用户名查找用户
        User user = getByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 2. 校验密码（BCrypt加密验证）
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        // 3. 生成 JWT Token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 4. 构建响应对象（不返回密码）
        user.setPassword(null);
        LoginResponse response = new LoginResponse();
        response.setUser(user);
        response.setToken(token);
        
        return response;
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    public Page<User> listAdminPage(Integer pageNum, Integer pageSize, String username, String phone, Integer status) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 按用户名模糊查询
        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username);
        }
        // 按手机号模糊查询
        if (StringUtils.hasText(phone)) {
            wrapper.like(User::getPhone, phone);
        }
        // 按状态查询
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public long getUserCount() {
        return count();
    }
}