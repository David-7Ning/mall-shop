package com.shopmall.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopmall.file.entity.FileInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {

    @Delete("DELETE FROM file_info WHERE storage_name = #{storageName}")
    int deleteByStorageName(@Param("storageName") String storageName);
}