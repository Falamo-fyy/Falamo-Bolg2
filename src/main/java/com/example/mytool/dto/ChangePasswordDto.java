package com.example.mytool.dto;

import javax.validation.constraints.*;
import lombok.Data;

@Data
public class ChangePasswordDto {
    
    @NotBlank(message = "当前密码不能为空")
    private String currentPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20个字符")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    // 自定义验证方法
    @AssertTrue(message = "新密码与确认密码不一致")
    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
} 