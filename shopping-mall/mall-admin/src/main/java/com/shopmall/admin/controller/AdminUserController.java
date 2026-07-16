package com.shopmall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopmall.admin.entity.User;
import com.shopmall.admin.feign.UserFeignClient;
import com.shopmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理 - 用户管理
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private UserFeignClient userFeignClient;

    /** 分页查询用户列表 */
    @GetMapping("/list")
    public Result<Page<User>> listAdminPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status) {
        return userFeignClient.listAdminPage(pageNum, pageSize, username, phone, status);
    }

    /** 禁用/启用用户 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return userFeignClient.updateUserStatus(id, status);
    }
}