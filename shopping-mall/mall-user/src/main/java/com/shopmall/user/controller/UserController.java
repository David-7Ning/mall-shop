package com.shopmall.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopmall.common.result.Result;
import com.shopmall.user.dto.LoginResponse;
import com.shopmall.user.entity.User;
import com.shopmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.shopmall.user.dto.RegisterDTO;
import com.shopmall.user.dto.LoginDTO;

/**
 * 用户Controller：处理用户相关的HTTP请求
 * 
 * @RestController = @Controller + @ResponseBody（返回JSON）
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

   

    // public Result<Void> register(@RequestParam String username,
    // @RequestParam String password) {
    // userService.register(username, password);
    // return Result.success();
    // }
 /** 用户注册：POST /user/register */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO.getUsername(), registerDTO.getPassword());
        return Result.success();
    }

    /** 用户登录：POST /user/login */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginDTO loginDTO) {
        LoginResponse response = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        return Result.success(response);
    }


    /** 查询用户信息：GET /user/{id} */
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }

    // ========== 后台管理接口 ==========

    /** 分页查询用户列表：GET /user/admin/list */
    @GetMapping("/admin/list")
    public Result<Page<User>> listAdminPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status) {
        return Result.success(userService.listAdminPage(pageNum, pageSize, username, phone, status));
    }

    /** 禁用/启用用户：PUT /user/admin/{id}/status */
    @PutMapping("/admin/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success();
    }

    /** 获取用户总数：GET /user/admin/count */
    @GetMapping("/admin/count")
    public Result<Long> getUserCount() {
        return Result.success(userService.getUserCount());
    }
}