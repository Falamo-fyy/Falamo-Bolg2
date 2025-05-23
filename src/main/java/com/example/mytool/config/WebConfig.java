package com.example.mytool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;


/**
 * Web MVC配置类。
 * 技术亮点：
 * 1. 配置静态资源映射，支持前后端分离和高效资源访问。
 * 2. 新增头像上传路径映射，结合MinIO/本地存储实现用户头像管理。
 * 3. 注册HiddenHttpMethodFilter，支持RESTful风格的表单请求（如PUT/DELETE）。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        // 新增头像资源映射
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }

    @Bean
    public FilterRegistrationBean<HiddenHttpMethodFilter> hiddenHttpMethodFilter() {
        FilterRegistrationBean<HiddenHttpMethodFilter> registrationBean = new FilterRegistrationBean<>(new HiddenHttpMethodFilter());
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}