package com.example.mytool.controller;

import com.example.mytool.dto.ArticleDto;
import com.example.mytool.service.HotArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class HotArticleController {

    @Autowired
    private HotArticleService hotArticleService;

    /**
     * 热门文章页面
     */
    @GetMapping("/hot-articles")
    public String hotArticlesPage(Model model) {
        List<ArticleDto> hotArticles = hotArticleService.getHotArticles();
        model.addAttribute("hotArticles", hotArticles);
        return "hot-articles"; // 对应热门文章页面模板
    }

    /**
     * 热门文章API
     */
    @GetMapping("/api/hot-articles")
    @ResponseBody
    public List<ArticleDto> getHotArticles() {
        return hotArticleService.getHotArticles();
    }
}