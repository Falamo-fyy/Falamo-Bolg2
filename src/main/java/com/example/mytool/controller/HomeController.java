package com.example.mytool.controller;

import com.example.mytool.entity.Article;
import com.example.mytool.entity.User;
import com.example.mytool.repository.UserRepository;
import com.example.mytool.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ArticleService articleService;

    /**
     * 处理直接表单提交（备选方案）
     */
    @PostMapping("/api/articles/article/create")
    public String createArticleDirectSubmit(
        @RequestParam String title,
        @RequestParam String content,
        @RequestParam String category,
        @AuthenticationPrincipal UserDetails userDetails,
        RedirectAttributes redirectAttributes) {

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
                redirectAttributes.addFlashAttribute("error", "无效的分类值: " + category);
                return "redirect:/article/editor";
            }
            
            article.setAuthor(author);
            article.setViews(0);
            article.setCreatedAt(new Date());
            article.setUpdatedAt(new Date());
            
            // 保存文章
            Article savedArticle = articleService.createArticle(article);
            System.out.println("文章创建成功，ID: " + savedArticle.getId());
            
            redirectAttributes.addFlashAttribute("success", "文章创建成功");
            return "redirect:/user/articles";
            
        } catch (Exception e) {
            System.out.println("直接提交创建文章失败: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "创建文章失败: " + e.getMessage());
            return "redirect:/article/editor";
        }
    }
} 