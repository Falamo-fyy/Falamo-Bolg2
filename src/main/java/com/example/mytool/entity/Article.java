package com.example.mytool.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Getter
@Setter
@Entity
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
    private Date createdAt;
    
    @Column(name = "updated_at")
    private Date updatedAt;
    
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
}
