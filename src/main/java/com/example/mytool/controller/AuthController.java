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

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(
        @ModelAttribute("user") @Valid UserRegistrationDto registrationDto,
        BindingResult result,
        RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "auth/register";
        }
        
        try {
            userService.registerUser(registrationDto);
            redirectAttributes.addFlashAttribute("success", "注册成功，请登录");
            return "redirect:/login";
        } catch (UsernameExistsException e) {
            result.rejectValue("username", "user.exists", e.getMessage());
        } catch (EmailExistsException e) {
            result.rejectValue("email", "email.exists", e.getMessage());
        }
        return "auth/register";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }
} 