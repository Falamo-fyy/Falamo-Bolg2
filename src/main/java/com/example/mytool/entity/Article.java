package com.example.mytool.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.time.LocalDateTime;

import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Getter
@Setter
@Entity
@Table(indexes = @Index(columnList = "title,content", name = "idx_search_fields"))
public class Article implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    @Lob
    private String content;
    
    @ManyToOne
    @JoinColumn(name = "author_id")
    @JsonBackReference
    private User author;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    private Integer views;
    private Integer likes = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
    
    public enum Category {
        TECHNOLOGY("技术"),
        LIFE("生活"),
        LEARNING("学习"),
        OTHER("其他");
        
        private final String displayName;
        
        Category(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
