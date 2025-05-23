package com.example.mytool.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * 定时任务配置类。
 * 技术亮点：
 * 1. 通过@EnableScheduling注解，轻松启用Spring Boot的定时任务调度能力。
 * 2. 支持灵活的定时任务开发，便于扩展定时业务逻辑。
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 启用定时任务
}