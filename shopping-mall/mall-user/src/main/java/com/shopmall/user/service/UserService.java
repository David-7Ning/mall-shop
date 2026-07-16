package com.shopmall.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shopmall.user.dto.LoginResponse;
import com.shopmall.user.entity.User;

/**
 * 用户Service接口
 * 继承IService后，自动拥有更多便捷方法
 */
public interface UserService extends IService<User> {

    /** 用户注册 */
    void register(String username, String password);

    /** 用户登录，返回登录响应（包含用户信息和 Token） */
    LoginResponse login(String username, String password);

    /** 根据用户名查询用户 */
    User getByUsername(String username);

    /** 后台管理：分页查询用户列表 */
    Page<User> listAdminPage(Integer pageNum, Integer pageSize, String username, String phone, Integer status);

    /** 后台管理：禁用/启用用户 */
    void updateUserStatus(Long id, Integer status);

    /** 后台管理：获取用户总数 */
    long getUserCount();
}