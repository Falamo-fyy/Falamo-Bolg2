package com.example.mytool.controller;

import com.example.mytool.entity.User;
import com.example.mytool.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    
    /**
     * 处理批量删除用户请求
     * @param userIds 要删除的用户ID数组，以逗号分隔
     * @param redirectAttributes 重定向属性
     * @return 重定向到用户管理页面
     */
    @PostMapping("/users/batch-delete")
    public String batchDeleteUsers(@RequestParam("userIds") String userIds, RedirectAttributes redirectAttributes) {
        try {
            if (userIds == null || userIds.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "未选择任何用户");
                return "redirect:/admin/users";
            }
            
            // 将逗号分隔的ID字符串转换为Long类型列表
            List<Long> userIdList = Arrays.stream(userIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            
            if (userIdList.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "未选择任何用户");
                return "redirect:/admin/users";
            }
            
            int deletedCount = userService.deleteUsers(userIdList);
            
            if (deletedCount == userIdList.size()) {
                redirectAttributes.addFlashAttribute("success", "成功删除 " + deletedCount + " 个用户");
            } else {
                redirectAttributes.addFlashAttribute("success", "成功删除 " + deletedCount + " 个用户，" + 
                        (userIdList.size() - deletedCount) + " 个用户无法删除（可能是管理员账户或不存在）");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "批量删除用户时发生错误: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    /**
     * 删除所有以testuser开头的测试用户
     * @param redirectAttributes 重定向属性
     * @return 重定向到用户管理页面
     */
    @PostMapping("/users/delete-test-users")
    public String deleteTestUsers(RedirectAttributes redirectAttributes) {
        try {
            int deletedCount = userService.deleteAllTestUsers();
            
            if (deletedCount > 0) {
                redirectAttributes.addFlashAttribute("success", "成功删除 " + deletedCount + " 个测试用户");
            } else {
                redirectAttributes.addFlashAttribute("info", "未找到以'testuser'开头的测试用户");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除测试用户时发生错误: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}