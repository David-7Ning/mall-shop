package com.shopmall.file.service;

import com.shopmall.file.entity.FileInfo;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileInfo uploadFile(MultipartFile file);

    void deleteFile(String storageName);

    String getFileUrl(String storageName);
}