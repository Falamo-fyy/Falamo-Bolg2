package com.example.mytool.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * MinIO对象存储配置类。
 * 技术亮点：
 * 1. 集成MinIO实现分布式对象存储，支持大文件上传与管理，提升文件管理灵活性和安全性。
 * 2. 通过@Value注解灵活读取配置，便于多环境切换。
 * 3. 统一提供MinioClient Bean，方便全局依赖注入和扩展。
 */
@Configuration
public class MinioConfig {
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}