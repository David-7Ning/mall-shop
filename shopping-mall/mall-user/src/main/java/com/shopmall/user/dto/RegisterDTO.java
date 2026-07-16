package com.shopmall.user.dto;
import lombok.Data;

/**
 * 注册请求 DTO
 */
@Data
public class RegisterDTO {
    /** 用户名 */
    private String username;
    
    /** 密码 */
    private String password;
    
    /** 昵称（选填） */
    private String nickname;
}
