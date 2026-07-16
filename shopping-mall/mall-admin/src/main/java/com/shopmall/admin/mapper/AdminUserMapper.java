package com.shopmall.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopmall.admin.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员 Mapper
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
}