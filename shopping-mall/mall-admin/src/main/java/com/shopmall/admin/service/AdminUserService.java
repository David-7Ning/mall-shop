package com.shopmall.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shopmall.admin.dto.AdminLoginResponse;
import com.shopmall.admin.entity.AdminUser;

/**
 * 管理员 Service 接口
 */
public interface AdminUserService extends IService<AdminUser> {

    /** 管理员登录 */
    AdminLoginResponse login(String username, String password);

    /** 根据用户名查询管理员 */
    AdminUser getByUsername(String username);
}