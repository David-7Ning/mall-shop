package com.shopmall.admin.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopmall.admin.dto.AdminLoginResponse;
import com.shopmall.admin.entity.AdminUser;
import com.shopmall.admin.mapper.AdminUserMapper;
import com.shopmall.admin.service.AdminUserService;
import com.shopmall.common.exception.BusinessException;
import com.shopmall.common.result.ResultCode;
import com.shopmall.common.util.JwtUtil;
import org.springframework.stereotype.Service;

/**
 * 管理员 Service 实现类
 */
@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {

    @Override
    public AdminLoginResponse login(String username, String password) {
        // 1. 根据用户名查找管理员
        AdminUser admin = getByUsername(username);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        // 2. 检查状态
        if (admin.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        // 3. 校验密码
        if (!BCrypt.checkpw(password, admin.getPassword())) {
            throw new BusinessException("密码错误");
        }
        // 4. 生成 JWT Token
        String token = JwtUtil.generateToken(admin.getId(), admin.getUsername());

        // 5. 构建响应（不返回密码）
        admin.setPassword(null);
        AdminLoginResponse response = new AdminLoginResponse();
        response.setAdmin(admin);
        response.setToken(token);

        return response;
    }

    @Override
    public AdminUser getByUsername(String username) {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminUser::getUsername, username);
        return getOne(wrapper);
    }
}