package com.example.mytool.controller;

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

import javax.validation.Valid;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Controller
@RequestMapping("/user")
public class ProfileController {

    private final UserService userService;
    private final StatsService statsService; // 注入 StatsService

    public ProfileController(UserService userService, StatsService statsService) {
        this.userService = userService;
        this.statsService = statsService;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserProfileDto profile = userService.getUserProfile(userDetails.getUsername());
        model.addAttribute("profile", profile);

        // 获取 stats 对象并添加到模型中
        Object stats = statsService.getStats(userDetails.getUsername());
        model.addAttribute("stats", stats);

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
        return "user/edit-profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute("profile") UserProfileDto profileDto,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "user/edit-profile";
        }

        try {
            userService.updateUserProfile(userDetails.getUsername(), profileDto);
            redirectAttributes.addFlashAttribute("success", "资料更新成功");
            return "redirect:/user/profile";
        } catch (EmailExistsException e) {
            result.rejectValue("email", "email.exists", e.getMessage());
            return "user/edit-profile";
        }
    }
}