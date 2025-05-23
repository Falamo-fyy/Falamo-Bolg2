package com.example.mytool.controller;

import com.example.mytool.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 文章点赞控制器
 * 处理文章点赞和取消点赞的API请求
 */
@RestController
@RequestMapping("/api/articles")
public class ArticleLikeController {
    
    /**
     * 点赞服务，处理点赞相关业务逻辑
     */
    private final LikeService likeService;

    @Autowired
    public ArticleLikeController(LikeService likeService) {
        this.likeService = likeService;
    }
    
    /**
     * 点赞文章
     * 处理用户对文章的点赞请求
     * 
     * @param id 文章ID
     * @param authentication 用户认证信息
     * @return 点赞操作结果
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeArticle(@PathVariable Long id, Authentication authentication) {
        // 检查用户是否已登录
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("请先登录");
        }
        
        // 获取当前用户名
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        // 调用服务执行点赞操作
        boolean success = likeService.likeArticle(username, id);
        
        // 构建响应数据
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        if (success) {
            response.put("message", "点赞成功");
        } else {
            response.put("message", "您已经点赞过这篇文章");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 取消点赞
     * 处理用户取消对文章点赞的请求
     * 
     * @param id 文章ID
     * @param authentication 用户认证信息
     * @return 取消点赞操作结果
     */
    @PostMapping("/{id}/unlike")
    public ResponseEntity<?> unlikeArticle(@PathVariable Long id, Authentication authentication) {
        // 检查用户是否已登录
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("请先登录");
        }
        
        // 获取当前用户名
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        // 调用服务执行取消点赞操作
        boolean success = likeService.unlikeArticle(username, id);
        
        // 构建响应数据
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "取消点赞成功" : "取消点赞失败");
        
        return ResponseEntity.ok(response);
    }
}