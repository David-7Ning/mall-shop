package com.shopmall.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopmall.user.entity.Address;
import com.shopmall.user.mapper.AddressMapper;
import com.shopmall.user.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收货地址服务实现类
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public List<Address> listByUserId(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)  // 默认地址排在前面
                .orderByDesc(Address::getCreateTime);
        return list(wrapper);
    }

    @Override
    @Transactional
    public Long addAddress(Address address) {
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        
        // 如果是第一个地址，自动设为默认地址
        if (address.getIsDefault() == null) {
            Long count = count(new LambdaQueryWrapper<Address>().eq(Address::getUserId, address.getUserId()));
            address.setIsDefault(count == 0 ? 1 : 0);
        }
        
        // 如果设为默认地址，取消其他默认地址
        if (address.getIsDefault() == 1) {
            clearDefault(address.getUserId());
        }
        
        save(address);
        return address.getId();
    }

    @Override
    @Transactional
    public void updateAddress(Address address) {
        address.setUpdateTime(LocalDateTime.now());
        
        // 如果设为默认地址，取消其他默认地址
        if (address.getIsDefault() == 1) {
            clearDefault(address.getUserId());
        }
        
        updateById(address);
    }

    @Override
    public void deleteAddress(Long addressId) {
        removeById(addressId);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        // 取消所有默认地址
        clearDefault(userId);
        
        // 设置新的默认地址
        Address address = new Address();
        address.setId(addressId);
        address.setIsDefault(1);
        address.setUpdateTime(LocalDateTime.now());
        updateById(address);
    }

    /**
     * 取消用户的所有默认地址
     */
    private void clearDefault(Long userId) {
        LambdaUpdateWrapper<Address> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
                .set(Address::getIsDefault, 0);
        update(wrapper);
    }
}