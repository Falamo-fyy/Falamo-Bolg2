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

@RestController
@RequestMapping("/api/articles")
public class ArticleLikeController {
    
    @Autowired
    private LikeService likeService;
    
    /**
     * 点赞文章
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeArticle(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("请先登录");
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        boolean success = likeService.likeArticle(username, id);
        
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
     */
    @PostMapping("/{id}/unlike")
    public ResponseEntity<?> unlikeArticle(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("请先登录");
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        boolean success = likeService.unlikeArticle(username, id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "取消点赞成功" : "取消点赞失败");
        
        return ResponseEntity.ok(response);
    }
}