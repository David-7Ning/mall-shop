package com.shopmall.admin.controller;

import com.shopmall.admin.dto.AdminLoginRequest;
import com.shopmall.admin.dto.AdminLoginResponse;
import com.shopmall.admin.service.AdminUserService;
import com.shopmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员 Controller
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminUserService adminUserService;

    /** 管理员登录：POST /admin/login */
    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminUserService.login(request.getUsername(), request.getPassword());
        return Result.success(response);
    }
}