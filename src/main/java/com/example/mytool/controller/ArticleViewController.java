package com.example.mytool.controller;

import com.example.mytool.entity.Article;
import com.example.mytool.repository.UserRepository;
import com.example.mytool.service.ArticleService;
import com.example.mytool.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * 文章视图控制器
 * 处理文章相关页面的渲染，包括搜索结果和文章详情页
 */
@Controller
    @RequestMapping("/article")
    public class ArticleViewController {

        private final ArticleService articleService;
        private final UserRepository userRepository;
        private final LikeService likeService;

        @Autowired
        public ArticleViewController(ArticleService articleService, UserRepository userRepository, LikeService likeService) {
            this.articleService = articleService;
            this.userRepository = userRepository;
            this.likeService = likeService;
        }


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
    
        /**
         * 显示文章详情页面
         * @param id 文章ID
         * @param model 模型
         * @return 视图名称
         */
        @GetMapping("/{id}")
        public String viewArticle(@PathVariable Long id, Model model, Authentication authentication) {
            try {
                System.out.println("正在尝试获取文章，ID: " + id);
                
                // 获取文章并增加阅读量
                Article article = articleService.getArticleWithStats(id);
                
                System.out.println("成功获取文章: " + article.getTitle());
                
                // 添加文章到模型
                model.addAttribute("article", article);
                
                // 添加作者信息
                model.addAttribute("author", article.getAuthor());
                
                // 添加是否点赞的标志，默认为false
                boolean hasLiked = false;
                
                // 如果用户已登录，检查是否已点赞
                if (authentication != null && authentication.isAuthenticated() && 
                    !authentication.getPrincipal().equals("anonymousUser")) {
                    // 获取当前用户
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                    // 检查用户是否已点赞
                    hasLiked = likeService.hasUserLikedArticle(userDetails.getUsername(), id);
                }
                
                model.addAttribute("hasLiked", hasLiked);
                
                // 判断当前用户是否是作者
                boolean isAuthor = false;
                if (authentication != null && authentication.isAuthenticated() && 
                    !authentication.getPrincipal().equals("anonymousUser")) {
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                    isAuthor = article.getAuthor().getUsername().equals(userDetails.getUsername());
                }
                model.addAttribute("isAuthor", isAuthor);
                
                System.out.println("准备渲染模板: article/detail");
                
                return "article/detail";
            } catch (Exception e) {
                // 捕获所有异常并打印详细信息
                System.err.println("获取文章详情时发生错误: " + e.getMessage());
                e.printStackTrace();
                
                // 文章不存在，返回404页面
                return "error/404";
            }
        }
    }
