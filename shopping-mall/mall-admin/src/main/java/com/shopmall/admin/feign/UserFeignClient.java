package com.shopmall.admin.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopmall.admin.entity.User;
import com.shopmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务 Feign 客户端
 */
@FeignClient(name = "mall-user", path = "/user")
public interface UserFeignClient {

    /** 分页查询用户列表 */
    @GetMapping("/admin/list")
    Result<Page<User>> listAdminPage(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "status", required = false) Integer status);

    /** 禁用/启用用户 */
    @PutMapping("/admin/{id}/status")
    Result<Void> updateUserStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status);

    /** 获取用户总数 */
    @GetMapping("/admin/count")
    Result<Long> getUserCount();
}