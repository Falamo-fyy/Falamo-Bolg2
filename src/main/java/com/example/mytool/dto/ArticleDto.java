package com.example.mytool.dto;

import com.example.mytool.entity.Article;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ArticleDto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private Long authorId;
    private Date createdAt;
    private Date updatedAt;
    private Integer views;
    private String category;
    
    // 从实体转换为DTO的静态方法
    public static ArticleDto fromEntity(Article article) {
        if (article == null) return null;
        
        ArticleDto dto = new ArticleDto();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setContent(article.getContent());
        if (article.getAuthor() != null) {
            dto.setAuthorName(article.getAuthor().getUsername());
            dto.setAuthorId(article.getAuthor().getId());
        }
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());
        dto.setViews(article.getViews());
        if (article.getCategory() != null) {
            dto.setCategory(article.getCategory().getDisplayName());
        }
        
        return dto;
    }
} 