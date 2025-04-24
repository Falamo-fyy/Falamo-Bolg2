package com.example.mytool.controller;

import com.example.mytool.entity.Article;
import com.example.mytool.repository.UserRepository;
import com.example.mytool.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
    @RequestMapping("/articles")
    public class ArticleViewController {
        
        @Autowired
        private ArticleService articleService;
        
        @Autowired
        private UserRepository userRepository;

        /**
         * 处理搜索请求并显示搜索结果
         */
        @GetMapping("/search")
        public String searchArticles(
                @RequestParam String keyword,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size,
                Model model) {
            
            Page<Article> searchResults = articleService.searchArticles(keyword, PageRequest.of(page, size));
            
            model.addAttribute("articles", searchResults.getContent());
            model.addAttribute("keyword", keyword);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", searchResults.getTotalPages());
            model.addAttribute("totalItems", searchResults.getTotalElements());
            model.addAttribute("pageSize", size);
            
            return "article/search-results";
        }
    }
