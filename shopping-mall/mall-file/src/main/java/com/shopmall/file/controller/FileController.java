package com.shopmall.file.controller;

import com.shopmall.common.result.Result;
import com.shopmall.file.entity.FileInfo;
import com.shopmall.file.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public Result<FileInfo> upload(@RequestParam("file") MultipartFile file) {
        FileInfo fileInfo = fileService.uploadFile(file);
        return Result.success(fileInfo);
    }

    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam("storageName") String storageName) {
        fileService.deleteFile(storageName);
        return Result.success();
    }

    @GetMapping("/url")
    public Result<String> getUrl(@RequestParam("storageName") String storageName) {
        String url = fileService.getFileUrl(storageName);
        return Result.success(url);
    }
}