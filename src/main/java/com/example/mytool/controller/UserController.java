package com.example.mytool.controller;

import com.example.mytool.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 用户控制器
 * 处理根路径下的用户相关请求
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 处理根路径下的用户删除请求
     * 将请求重定向到管理员控制器的删除用户端点
     * @param userId 要删除的用户ID
     * @param redirectAttributes 重定向属性
     * @return 重定向到管理员的用户管理页面
     */
    @GetMapping("/{userId}/delete")
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
}