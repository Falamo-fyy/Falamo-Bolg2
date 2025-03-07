package com.example.mytool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = {
    "com.example.mytool",
    "com.example.mytool.demos.web" // 添加控制器所在包
})
@EnableCaching
public class MytoolApplication {

    public static void main(String[] args) {
        SpringApplication.run(MytoolApplication.class, args);
    }
}