package com.example.mytool.controller;

import com.example.mytool.entity.User;
import com.example.mytool.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 管理员控制器
 * 处理管理员相关功能，如用户管理
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 显示用户管理页面
     * @param model 模型
     * @return 用户管理页面视图
     */
    @GetMapping("/users")
    public String userManagement(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/user-management";
    }

    /**
     * 处理删除用户请求 (POST方法)
     * @param userId 要删除的用户ID
     * @param redirectAttributes 重定向属性
     * @return 重定向到用户管理页面
     */
    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("success", "用户删除成功");
        } catch (UsernameNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "用户不存在");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除用户时发生错误: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    /**
     * 处理删除用户请求 (GET方法)
     * 注意：通常删除操作应该使用POST方法，这里添加GET方法是为了兼容现有请求
     * @param userId 要删除的用户ID
     * @param redirectAttributes 重定向属性
     * @return 重定向到用户管理页面
     */
    @GetMapping("/users/{userId}/delete")
    public String deleteUserGet(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        return deleteUser(userId, redirectAttributes);
    }
    
}