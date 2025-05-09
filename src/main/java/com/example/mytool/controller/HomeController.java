package com.example.mytool.controller;

import com.example.mytool.dto.ArticleUpdateDto;
import com.example.mytool.entity.Article;
import com.example.mytool.entity.User;
import com.example.mytool.repository.UserRepository;
import com.example.mytool.service.ArticleService;
import com.example.mytool.service.HotArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * 首页控制器
 * 处理网站首页和文章创建的请求
 */
@Controller
public class HomeController {

    /**
     * 用户仓库，用于查询用户信息
     */
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 文章服务，处理文章相关业务逻辑
     */
    @Autowired
    private ArticleService articleService;
    
    /**
     * 热门文章服务，处理热门文章相关业务逻辑
     */
    @Autowired
    private HotArticleService hotArticleService;

    /**
     * 处理直接表单提交（备选方案）
     * 创建新文章的API端点
     * 
     * @param requestData 文章标题
     * @param userDetails 当前登录用户
     * @return 创建结果
     */
    @ResponseBody
    @PostMapping("/api/articles/article/create")
    public ResponseEntity<?> createArticleDirectSubmit(
        @RequestBody Map<String, String> requestData,
        @AuthenticationPrincipal UserDetails userDetails) {
        
        String title = requestData.get("title");
        String content = requestData.get("content");
        String category = requestData.get("category");
        
        if (title == null || title.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "标题不能为空"));
        }

        try {
            System.out.println("收到直接表单提交请求");
            System.out.println("标题: " + title);
            System.out.println("分类: " + category);
            System.out.println("内容长度: " + (content != null ? content.length() : 0));
            
            // 获取当前用户
            User author = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
            
            System.out.println("找到用户: " + author.getUsername() + ", ID: " + author.getId());
            
            // 创建新文章
            Article article = new Article();
            article.setTitle(title);
            article.setContent(content);
            
            try {
                System.out.println("尝试解析分类: " + category);
                article.setCategory(Article.Category.valueOf(category));
            } catch (IllegalArgumentException e) {
                System.out.println("分类解析失败: " + e.getMessage());
                return ResponseEntity.badRequest().body("无效的分类值: " + category);
            }
            
            // 设置文章属性
            article.setAuthor(author);
            article.setViews(0);
            article.setCreatedAt(LocalDateTime.now());
            article.setUpdatedAt(LocalDateTime.now());
            
            // 保存文章
            Article savedArticle = articleService.createArticle(article);
            System.out.println("文章创建成功，ID: " + savedArticle.getId());
            
            return ResponseEntity.ok().body(Collections.singletonMap("redirectUrl", "/articles/" + savedArticle.getId()));
            
        } catch (Exception e) {
            System.out.println("直接提交创建文章失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("创建文章失败: " + e.getMessage());
        }
    }

    /**
     * 显示网站首页
     * 获取所有文章并分页显示
     * 
     * @param page 当前页码
     * @param size 每页显示数量
     * @param model 视图模型
     * @return 首页视图名
     */
    @GetMapping("/")
    public String home(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model) {
        
        // 获取所有文章并按照创建时间降序排序分页
        Page<Article> articles = articleService.getAllArticles(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        model.addAttribute("articles", articles);
        
        // 添加热门文章列表（仅前3篇）
        model.addAttribute("hotArticles", hotArticleService.getHotArticles().subList(0, 
            Math.min(3, hotArticleService.getHotArticles().size())));
        
        return "index"; // 对应你的首页模板
    }
}