package com.shopmall.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_info")
public class FileInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;

    private String storageName;

    private Long fileSize;

    private String fileType;

    private String fileUrl;

    private String bucketName;

    private Long uploadUserId;

    private LocalDateTime uploadTime;

    private Integer downloadCount;

    private Integer status;

    private String remark;
}