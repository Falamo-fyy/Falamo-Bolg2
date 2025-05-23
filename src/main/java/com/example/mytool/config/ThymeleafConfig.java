package com.example.mytool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;


/**
 * Thymeleaf模板引擎配置类。
 * 技术亮点：
 * 1. 集成Thymeleaf模板引擎，实现后端动态网页渲染，提升开发效率和页面交互体验。
 * 2. 通过扩展Java8TimeDialect，支持Java 8时间API在模板中的友好显示。
 * 3. 配置灵活，便于后续自定义和扩展。
 */
@Configuration
public class ThymeleafConfig {
    @Bean
    public Java8TimeDialect java8TimeDialect() {
        return new Java8TimeDialect();
    }
}