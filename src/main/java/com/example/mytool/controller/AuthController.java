package com.example.mytool.controller;

import com.example.mytool.dto.UserRegistrationDto;
import com.example.mytool.exception.EmailExistsException;
import com.example.mytool.exception.UsernameExistsException;
import com.example.mytool.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

/**
 * 认证控制器
 * 处理用户注册和登录相关的请求
 */
@Controller
public class AuthController {

    /**
     * 用户服务，处理用户相关业务逻辑
     */
    @Autowired
    private UserService userService;

    /**
     * 显示注册表单页面
     * 
     * @param model 视图模型
     * @return 注册页面视图名
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // 添加空的注册DTO到模型
        model.addAttribute("user", new UserRegistrationDto());
        return "auth/register";
    }

    /**
     * 处理用户注册请求
     * 
     * @param registrationDto 注册数据传输对象
     * @param result 表单验证结果
     * @param redirectAttributes 重定向属性
     * @return 重定向到登录页或返回注册页（出错时）
     */
    @PostMapping("/register")
    public String registerUser(
        @ModelAttribute("user") @Valid UserRegistrationDto registrationDto,
        BindingResult result,
        RedirectAttributes redirectAttributes) {
        
        // 检查表单验证是否有错误
        if (result.hasErrors()) {
            return "auth/register";
        }
        
        try {
            // 调用服务注册用户
            userService.registerUser(registrationDto);
            // 添加成功消息
            redirectAttributes.addFlashAttribute("success", "注册成功，请登录");
            // 重定向到登录页，添加registered=true参数
            return "redirect:/login?registered=true";
        } catch (UsernameExistsException e) {
            // 用户名已存在
            result.rejectValue("username", "user.exists", e.getMessage());
        } catch (EmailExistsException e) {
            // 邮箱已存在
            result.rejectValue("email", "email.exists", e.getMessage());
        }
        return "auth/register";
    }

    /**
     * 显示登录表单页面
     * 
     * @return 登录页面视图名
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }
}