package com.example.mytool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * 通用应用配置类。
 * 技术亮点：
 * 1. 统一配置全局PasswordEncoder，保障用户密码安全，配合Spring Security实现高强度加密。
 * 2. 采用@Bean方式，便于后续扩展和维护。
 */
@Configuration
public class AppConfig {
    // 唯一PasswordEncoder定义
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}