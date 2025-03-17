package com.example.mytool.dto;

import com.example.mytool.entity.Article;
import lombok.Data;

@Data
public class ArticleUpdateDto {
    private String title;
    private String content;
    private String category;
} 