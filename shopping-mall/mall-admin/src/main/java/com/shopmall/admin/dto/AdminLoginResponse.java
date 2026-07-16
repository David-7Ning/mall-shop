package com.shopmall.admin.dto;

import com.shopmall.admin.entity.AdminUser;
import lombok.Data;

/**
 * 管理员登录响应
 */
@Data
public class AdminLoginResponse {

    private AdminUser admin;

    private String token;
}