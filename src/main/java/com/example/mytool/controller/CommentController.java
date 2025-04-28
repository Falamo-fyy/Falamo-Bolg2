package com.example.mytool.controller;

import com.example.mytool.dto.CommentDto;
import com.example.mytool.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    /**
     * 获取文章评论
     */
    @GetMapping("/article/{articleId}")
    public List<CommentDto> getArticleComments(@PathVariable Long articleId) {
        return commentService.getArticleComments(articleId);
    }
    
    /**
     * 添加评论
     */
    @PostMapping("/article/{articleId}")
    public ResponseEntity<?> addComment(
            @PathVariable Long articleId,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String content = payload.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "评论内容不能为空"));
        }
        
        try {
            CommentDto commentDto = commentService.addComment(
                    articleId, userDetails.getUsername(), content);
            return ResponseEntity.status(HttpStatus.CREATED).body(commentDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            commentService.deleteComment(commentId, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}