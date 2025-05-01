package com.example.mytool.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "article_likes", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"article_id", "user_id"}))
public class ArticleLike {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // 构造函数
    public ArticleLike() {
    }
    
    public ArticleLike(Article article, User user) {
        this.article = article;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

}