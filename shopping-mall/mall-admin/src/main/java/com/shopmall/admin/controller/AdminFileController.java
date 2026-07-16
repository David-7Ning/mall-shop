package com.shopmall.admin.controller;

import com.shopmall.admin.feign.FileFeignClient;
import com.shopmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 后台管理 - 文件上传
 */
@RestController
@RequestMapping("/admin/file")
public class AdminFileController {

    @Autowired
    private FileFeignClient fileFeignClient;

    /** 上传文件 */
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        return fileFeignClient.upload(file);
    }
}