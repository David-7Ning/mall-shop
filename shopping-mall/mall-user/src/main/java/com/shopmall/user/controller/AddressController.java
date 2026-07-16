package com.shopmall.user.controller;

import com.shopmall.common.result.Result;
import com.shopmall.user.entity.Address;
import com.shopmall.user.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址控制器（RESTful 风格）
 */
@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    /** 查询用户的收货地址列表：GET /address/list?userId=1 */
    @GetMapping("/list")
    public Result<List<Address>> list(@RequestParam Long userId) {
        return Result.success(addressService.listByUserId(userId));
    }

    /** 添加收货地址：POST /address */
    @PostMapping
    public Result<Long> add(@RequestBody Address address) {
        Long id = addressService.addAddress(address);
        return Result.success(id);
    }

    /** 更新收货地址：PUT /address/{id} */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Address address) {
        address.setId(id);
        addressService.updateAddress(address);
        return Result.success();
    }

    /** 删除收货地址：DELETE /address/{id} */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return Result.success();
    }

    /** 设置默认地址：PUT /address/{id}/default */
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id, @RequestParam Long userId) {
        addressService.setDefaultAddress(userId, id);
        return Result.success();
    }
}