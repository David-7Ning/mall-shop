package com.shopmall.user.dto;
import lombok.Data;

/**
 * 
 * 登录请求Dto
 */
@Data
public class LoginDTO {
    /**用户名 */
    private String username;
    
    /** 密码 */
    private String password;
}
