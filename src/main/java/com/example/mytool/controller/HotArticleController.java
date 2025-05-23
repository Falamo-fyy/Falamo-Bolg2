package com.example.mytool.controller;

import com.example.mytool.dto.ArticleDto;
import com.example.mytool.service.HotArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Controller
public class HotArticleController {

    private final HotArticleService hotArticleService;

    @Autowired
    public HotArticleController(HotArticleService hotArticleService) {
        this.hotArticleService = hotArticleService;
    }

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
    
    /**
     * 手动刷新热门文章缓存
     */
    @PostMapping("/api/hot-articles/refresh")
    @ResponseBody
    public Map<String, Object> refreshHotArticles() {
        List<ArticleDto> refreshedArticles = hotArticleService.refreshHotArticlesCache();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "热门文章榜单已刷新");
        response.put("articles", refreshedArticles);
        return response;
    }
}