package com.example.mytool.config;

import io.minio.MinioClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestMinioConfig {
    
    @Bean
    @Primary
    public MinioClient minioClient() {
        // 使用测试环境的 Minio 配置
        return MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("minioadmin", "minioadmin")
                .build();
    }
}