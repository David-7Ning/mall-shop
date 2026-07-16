package com.shopmall.file.service;

import com.shopmall.file.entity.FileInfo;
import com.shopmall.file.mapper.FileInfoMapper;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;
    private final FileInfoMapper fileInfoMapper;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    public FileServiceImpl(MinioClient minioClient, FileInfoMapper fileInfoMapper) {
        this.minioClient = minioClient;
        this.fileInfoMapper = fileInfoMapper;
    }

    @Override
    public FileInfo uploadFile(MultipartFile file) {
        try {
            // 1. 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String extension = originalName != null ? originalName.substring(originalName.lastIndexOf(".")) : "";
            String storageName = UUID.randomUUID().toString().replace("-", "") + extension;

            // 2. 上传到 MinIO
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(storageName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            // 3. 生成访问 URL
            String fileUrl = endpoint + "/" + bucketName + "/" + storageName;

            // 4. 保存文件元数据到数据库
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileName(originalName);
            fileInfo.setStorageName(storageName);
            fileInfo.setFileSize(file.getSize());
            fileInfo.setFileType(file.getContentType());
            fileInfo.setFileUrl(fileUrl);
            fileInfo.setBucketName(bucketName);
            fileInfo.setStatus(1);
            fileInfoMapper.insert(fileInfo);

            log.info("文件上传成功: {} -> {}", originalName, storageName);
            return fileInfo;

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String storageName) {
        try {
            // 1. 从 MinIO 删除文件
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageName)
                            .build()
            );

            // 2. 从数据库删除记录
            fileInfoMapper.deleteByStorageName(storageName);

            log.info("文件删除成功: {}", storageName);

        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String storageName) {
        try {
            // 生成预签名 URL（7 天有效期）
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(storageName)
                            .expiry(60 * 60 * 24 * 7) // 7 天
                            .build()
            );
            return url;

        } catch (Exception e) {
            log.error("获取文件 URL 失败", e);
            throw new RuntimeException("获取文件 URL 失败: " + e.getMessage());
        }
    }
}