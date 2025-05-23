package com.example.mytool.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Getter
@Setter
@Entity
@Table(indexes = @Index(columnList = "title,content", name = "idx_search_fields"))
public class Article implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    @Lob
    @Column(columnDefinition = "VARCHAR(255)")
    private String content;
    
    @ManyToOne
    @JoinColumn(name = "author_id")
    @JsonBackReference
    private User author;
    
    @Setter
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Setter
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    private Integer views;
    
    @Setter
    @Getter
    @Column(name = "likes", nullable = false)
    private Integer likes = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
    
    // 添加评论关联
    @JsonManagedReference
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
    
    // 添加文章初始化方法
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.views == null) {
            this.views = 0;
        }
        if (this.likes == null) {
            this.likes = 0;
        }
    }
    
    @Getter
    public enum Category {
        TECHNOLOGY("技术"),
        LIFE("生活"),
        LEARNING("学习"),
        OTHER("其他");
        
        private final String displayName;
        
        Category(String displayName) {
            this.displayName = displayName;
        }
    }
}
