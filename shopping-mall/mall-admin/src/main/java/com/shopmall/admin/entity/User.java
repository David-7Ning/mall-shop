package com.shopmall.admin.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类（与 mall-user 保持一致）
 */
@Data
public class User {

    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 状态：0禁用 1正常 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}