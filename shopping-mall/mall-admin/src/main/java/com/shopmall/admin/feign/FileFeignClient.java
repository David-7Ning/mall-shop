package com.shopmall.admin.feign;

import com.shopmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件服务 Feign 客户端
 */
@FeignClient(name = "mall-file", path = "/file")
public interface FileFeignClient {

    /** 上传文件 */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<Map<String, Object>> upload(@RequestPart("file") MultipartFile file);

    /** 删除文件 */
    @DeleteMapping("/delete")
    Result<Void> delete(@RequestParam("storageName") String storageName);

    /** 获取文件 URL */
    @GetMapping("/url")
    Result<String> getUrl(@RequestParam("storageName") String storageName);
}