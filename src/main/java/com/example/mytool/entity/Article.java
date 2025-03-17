package com.example.mytool.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

import javax.persistence.Enumerated;
import javax.persistence.EnumType;

@Getter
@Setter
@Entity
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    @Lob
    private String content;
    
    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;
    
    @Column(name = "created_at")
    private Date createdAt;
    
    @Column(name = "updated_at")
    private Date updatedAt;
    
    private Integer views;
    
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
}
