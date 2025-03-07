package com.example.mytool.dto;

import lombok.Data;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
public class UserProfileDto {
    private String username;
    private String email;
    private String bio;
    private String avatarUrl;
    private Date createdAt;  // 保持Date类型

    // 添加转换方法（不需要setter）
    public LocalDateTime getCreatedAtAsLocalDateTime() {
        return this.createdAt.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
    // 添加 setCreatedAtAsLocalDateTime 方法
    public void setCreatedAtAsLocalDateTime(LocalDateTime createdAt) {
        // 这里可以根据需要将 LocalDateTime 转换回 Date 类型
        this.createdAt = Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant());
    }
}