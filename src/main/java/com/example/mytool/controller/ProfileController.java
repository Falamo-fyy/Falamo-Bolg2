package com.example.mytool.controller;

import com.example.mytool.dto.ChangePasswordDto;
import com.example.mytool.dto.UserProfileDto;
import com.example.mytool.exception.EmailExistsException;
import com.example.mytool.service.StatsService;
import com.example.mytool.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.example.mytool.entity.User;
import com.example.mytool.repository.UserRepository;
import com.example.mytool.exception.InvalidPasswordException;
import java.security.Principal;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.util.UUID;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

import javax.validation.Valid;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/user")
public class ProfileController {

    private final UserService userService;
    private final StatsService statsService; // 注入 StatsService
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Value("${upload.path}")
    private String uploadPath;

    public ProfileController(UserService userService, StatsService statsService, UserRepository userRepository) {
        this.userService = userService;
        this.statsService = statsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        
        model.addAttribute("profile", userService.getUserProfile(user.getUsername()));
        model.addAttribute("stats", userService.getUserStats(user.getId()));
        return "user/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfileForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        UserProfileDto profile = userService.getUserProfile(userDetails.getUsername());
        // 将 Timestamp 转换为 LocalDateTime
        if (profile.getCreatedAt() != null) {
            LocalDateTime createdAt = profile.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            profile.setCreatedAtAsLocalDateTime(createdAt);
        }
        model.addAttribute("profile", profile);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute("profile") UserProfileDto profileDto,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "user/profile";
        }

        try {
            userService.updateUserProfile(userDetails.getUsername(), profileDto);
            redirectAttributes.addFlashAttribute("success", "资料更新成功");
            return "redirect:/user/profile?success=true";
        } catch (EmailExistsException e) {
            result.rejectValue("email", "email.exists", e.getMessage());
            return "user/profile";
        }
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("changePasswordForm", new ChangePasswordDto());
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String handlePasswordChange(
        @Valid @ModelAttribute("changePasswordForm") ChangePasswordDto changePasswordDto,
        BindingResult result,
        @AuthenticationPrincipal UserDetails userDetails,
        RedirectAttributes redirectAttributes) {

        logger.debug("Received password change request: {}", changePasswordDto);
        
        // 手动验证密码匹配
        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
            logger.warn("Password mismatch detected");
            result.rejectValue("confirmPassword", "password.mismatch", "新密码与确认密码不一致");
        }

        if (result.hasErrors()) {
            logger.error("Validation errors: {}", result.getAllErrors());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.changePasswordForm", result);
            redirectAttributes.addFlashAttribute("changePasswordForm", changePasswordDto);
            return "redirect:/user/change-password";
        }

        try {
            userService.changePassword(
                userDetails.getUsername(),
                changePasswordDto.getCurrentPassword(),
                changePasswordDto.getNewPassword()
            );
            logger.info("Password changed successfully for user: {}", userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "密码修改成功");
        } catch (InvalidPasswordException e) {
            logger.error("Invalid current password", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.changePasswordForm", result);
            redirectAttributes.addFlashAttribute("changePasswordForm", changePasswordDto);
        } catch (IllegalArgumentException e) {
            logger.error("Validation failed", e);
            result.rejectValue("newPassword", "password.invalid", e.getMessage());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.changePasswordForm", result);
            redirectAttributes.addFlashAttribute("changePasswordForm", changePasswordDto);
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            redirectAttributes.addFlashAttribute("error", "系统错误，请稍后再试");
        }
        
        return "redirect:/user/change-password";
    }

    @PostMapping("/upload-avatar")
    public String uploadAvatar(
        @RequestParam("file") MultipartFile file,
        @AuthenticationPrincipal UserDetails userDetails,
        RedirectAttributes redirectAttributes) {
        
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
            
            // 验证文件类型
            if (file.isEmpty() || !file.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("请选择有效的图片文件");
            }

            // 生成唯一文件名
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadPath, filename);
            
            // 保存文件
            Files.createDirectories(filePath.getParent());
            file.transferTo(filePath.toFile());
            
            // 更新用户头像路径
            user.setAvatarUrl("/uploads/" + filename);
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("success", "头像更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "头像上传失败: " + e.getMessage());
        }
        return "redirect:/user/profile";
    }
}