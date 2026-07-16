package com.shopmall.user.dto;

import com.shopmall.user.entity.User;
import lombok.Data;

/**
 * 登录响应 DTO
 * 包含用户信息和 Token
 */
@Data
public class LoginResponse {

    /** 用户信息 */
    private User user;

    /** JWT Token */
    private String token;
}