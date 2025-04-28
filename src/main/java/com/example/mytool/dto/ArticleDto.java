package com.example.mytool.dto;

import com.example.mytool.entity.Article;
import lombok.Data;
import lombok.Setter;

import java.io.Serializable;
import java.time.ZoneId;
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
    private int likes;
    private int commentCount;
    // 添加是否已点赞字段，用于前端显示
    private boolean liked;

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

        if (article.getCreatedAt() != null) {
            dto.setCreatedAt(Date.from(article.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (article.getUpdatedAt() != null) {
            dto.setUpdatedAt(Date.from(article.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant()));
        }
        dto.setViews(article.getViews() != null ? article.getViews() : 0);
        if (article.getCategory() != null) {
            dto.setCategory(article.getCategory().getDisplayName());
        }
        
        // 设置点赞数
        dto.setLikes(article.getLikes() != null ? article.getLikes() : 0);
        
        // 设置评论数量
        if (article.getComments() != null) {
            dto.setCommentCount(article.getComments().size());
        }
        
        return dto;
    }
    
    // 添加一个重载方法，可以设置当前用户是否已点赞
    public static ArticleDto fromEntity(Article article, boolean liked) {
        ArticleDto dto = fromEntity(article);
        if (dto != null) {
            dto.setLiked(liked);
        }
        return dto;
    }
}