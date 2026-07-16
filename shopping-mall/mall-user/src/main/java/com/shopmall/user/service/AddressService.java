package com.shopmall.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shopmall.user.entity.Address;

import java.util.List;

/**
 * 收货地址服务接口
 */
public interface AddressService extends IService<Address> {

    /** 查询用户的收货地址列表 */
    List<Address> listByUserId(Long userId);

    /** 添加收货地址 */
    Long addAddress(Address address);

    /** 更新收货地址 */
    void updateAddress(Address address);

    /** 删除收货地址 */
    void deleteAddress(Long addressId);

    /** 设置默认地址 */
    void setDefaultAddress(Long userId, Long addressId);
}