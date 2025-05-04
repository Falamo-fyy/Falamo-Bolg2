package com.example.mytool.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 自定义错误控制器
 * 处理应用中的各种HTTP错误，提供友好的错误页面
 */
@Controller
public class CustomErrorController implements ErrorController {

    /**
     * 处理所有错误请求
     * @param request HTTP请求
     * @param model Spring MVC模型
     * @return 对应的错误页面视图名称
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // 获取错误状态码
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        // 添加错误信息到模型
        model.addAttribute("timestamp", new Date());
        model.addAttribute("path", path);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            model.addAttribute("status", statusCode);
            
            // 根据状态码返回对应的错误页面
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("error", "页面未找到");
                model.addAttribute("message", "您请求的页面不存在");
                return "error/404";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("error", "访问被拒绝");
                model.addAttribute("message", "您没有权限访问此页面");
                return "error/403";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("error", "服务器内部错误");
                model.addAttribute("message", message != null ? message.toString() : "服务器遇到了一个错误");
                return "error/500";
            }
        }
        
        // 默认错误信息
        model.addAttribute("error", "发生错误");
        model.addAttribute("message", message != null ? message.toString() : "处理您的请求时发生错误");
        
        // 返回通用错误页面
        return "error/error";
    }
}