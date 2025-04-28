package com.example.mytool.dto;

import com.example.mytool.entity.Comment;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Data
public class CommentDto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String content;
    private Long articleId;
    private Long authorId;
    private String authorName;
    private String authorAvatarUrl;
    private Date createdAt;
    
    // 从实体转换为DTO的静态方法
    public static CommentDto fromEntity(Comment comment) {
        if (comment == null) return null;
        
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        
        if (comment.getArticle() != null) {
            dto.setArticleId(comment.getArticle().getId());
        }
        
        if (comment.getAuthor() != null) {
            dto.setAuthorId(comment.getAuthor().getId());
            dto.setAuthorName(comment.getAuthor().getUsername());
            dto.setAuthorAvatarUrl(comment.getAuthor().getAvatarUrl());
        }
        
        if (comment.getCreatedAt() != null) {
            dto.setCreatedAt(Date.from(comment.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        return dto;
    }
}