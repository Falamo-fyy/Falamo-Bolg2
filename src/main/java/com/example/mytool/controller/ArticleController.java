package com.example.mytool.controller;

import com.example.mytool.dto.ArticleDto;
import com.example.mytool.dto.ArticleUpdateDto;
import com.example.mytool.entity.Article;
import com.example.mytool.entity.User;
import com.example.mytool.repository.UserRepository;
import com.example.mytool.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 文章相关API控制器
 * 提供文章的搜索、浏览量更新、用户文章列表获取、删除和更新等功能
 */
@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    /**
     * 文章服务，用于处理文章相关业务逻辑
     */
    @Autowired
    private ArticleService articleService;

    /**
     * 用户仓库，用于查询用户信息
     */
    @Autowired
    private UserRepository userRepository;  // 声明并注入 userRepository

    /**
     * 文章搜索API
     * @param keyword 搜索关键词
     * @param page 分页页码（从0开始）
     * @param size 每页数量
     * @return 分页结果（包含文章数据和分页信息）
     */
    @GetMapping("/search")
    public Page<Article> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return articleService.searchArticles(keyword, PageRequest.of(page, size));
    }

    /**
     * 增加文章浏览数
     * @param id 文章ID（路径变量）
     * @throws ResponseStatusException 当文章不存在时返回404状态
     */
    @PostMapping("/{id}/view")
    public void incrementViewCount(@PathVariable Long id) {
        try {
            // 调用服务层方法更新浏览数
            articleService.incrementViews(id);
        } catch (Exception e) {
            // 捕获服务层抛出的异常并转换为HTTP状态码
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "文章不存在", e);
        }
    }

    /**
     * 获取用户的文章列表
     * @param userId 用户ID
     * @param page 当前页码
     * @param size 每页数量
     * @return 分页的文章列表
     */
    @GetMapping("/user/{userId}")
    public Page<Article> getUserArticles(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return articleService.getUserArticles(userId, PageRequest.of(page, size));
    }
    
    /**
     * 删除文章
     * @param id 文章ID
     * @param userDetails 当前登录用户
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 获取当前用户信息
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        
        // 获取文章信息
        Article article = articleService.getArticleById(id);
        
        // 检查当前用户是否是文章作者
        if (!article.getAuthor().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            // 删除文章
            articleService.deleteArticle(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 更新文章
     * @param id 文章ID
     * @param updateDto 更新数据传输对象
     * @param userDetails 当前登录用户
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticle(
            @PathVariable Long id,
            @RequestBody ArticleUpdateDto updateDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 添加调试日志
        System.out.println("收到更新请求，文章ID: " + id);
        System.out.println("更新数据: " + updateDto);
        
        try {
            // 获取现有文章
            Article existingArticle = articleService.getArticleById(id);
            
            if (existingArticle == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 检查当前用户是否是文章作者
            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
                
            if (!existingArticle.getAuthor().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "您没有权限编辑此文章"));
            }
            
            // 更新文章内容
            if (updateDto.getTitle() != null) {
                existingArticle.setTitle(updateDto.getTitle());
            }
            
            if (updateDto.getContent() != null) {
                existingArticle.setContent(updateDto.getContent());
            }
            
            if (updateDto.getCategory() != null) {
                existingArticle.setCategory(Article.Category.valueOf(updateDto.getCategory()));
            }
            
            existingArticle.setUpdatedAt(LocalDateTime.now());
            
            // 保存更新后的文章
            Article savedArticle = articleService.updateArticle(id, existingArticle);
            return ResponseEntity.ok(Collections.singletonMap("id", savedArticle.getId()));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Collections.singletonMap("error", "无效的分类值: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "更新文章失败: " + e.getMessage()));
        }
    }

    /**
     * 用户文章管理控制器（嵌套类）
     * 处理用户文章列表和编辑页面的渲染
     */
    @Controller
    @RequestMapping("/user/articles")
    public class UserArticleControllerIndex {
        
        /**
         * 文章服务
         */
        @Autowired
        private ArticleService articleService;
        
        /**
         * 用户仓库
         */
        @Autowired
        private UserRepository userRepository;

        /**
         * 显示用户文章管理页面
         * @param page 当前页码
         * @param size 每页数量
         * @param userDetails 当前登录用户
         * @param model 视图模型
         * @return 视图名称
         */
        @GetMapping
        public String showUserArticles(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size,
                @AuthenticationPrincipal UserDetails userDetails,
                Model model) {
            
            // 获取当前用户
            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
            
            // 获取用户文章列表
            Page<ArticleDto> articles = articleService.getUserArticlesDtos(
                user.getId(), PageRequest.of(page, size));
            
            // 添加数据到模型
            model.addAttribute("articles", articles);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", articles.getTotalPages());
            
            return "user/articles";
        }
        
        /**
         * 显示文章编辑页面
         * @param id 文章ID
         * @param userDetails 当前登录用户
         * @param model 视图模型
         * @param redirectAttributes 重定向属性
         * @return 视图名称
         */
        @GetMapping("/edit/{id}")
        public String showEditArticle(
                @PathVariable Long id,
                @AuthenticationPrincipal UserDetails userDetails,
                Model model,
                RedirectAttributes redirectAttributes) {
            
            try {
                // 获取文章
                Article article = articleService.getArticleById(id);
                
                if (article == null) {
                    redirectAttributes.addFlashAttribute("error", "文章不存在");
                    return "redirect:/user/articles";
                }
                
                // 检查当前用户是否是文章作者
                User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
                    
                if (!article.getAuthor().getId().equals(user.getId())) {
                    redirectAttributes.addFlashAttribute("error", "您没有权限编辑此文章");
                    return "redirect:/user/articles";
                }
                
                // 确保将完整的文章对象添加到模型中
                model.addAttribute("article", article);
                return "article/editor";
                
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "加载文章失败: " + e.getMessage());
                return "redirect:/user/articles";
            }
        }

        // 新增编辑器页面路由
        @GetMapping("/editor")
        public String showEditorForm(
            @RequestParam(required = false) Long id,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
            
            if (id != null) {
                Article article = articleService.getArticleById(id);
                model.addAttribute("article", article);
            }
            return "article/editor";
        }
    }
    /**
     * 文章管理控制器（嵌套类，处理模板渲染）
     */
    @Controller
    @RequestMapping("/article")
    public class UserArticleController {
        
        @Autowired
        private ArticleService articleService;
        
        @Autowired
        private UserRepository userRepository;

        /**
         * 显示用户文章管理页面
         */
        @GetMapping
        public String showUserArticles(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size,
                @AuthenticationPrincipal UserDetails userDetails,
                Model model) {
            
            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
                
            Page<ArticleDto> articles = articleService.getUserArticlesDtos(
                user.getId(), PageRequest.of(page, size));
                
            model.addAttribute("articles", articles);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", articles.getTotalPages());
            
            return "user/articles";
        }
        
        /**
         * 显示文章编辑页面
         */
        @GetMapping("/edit/{id}")
        public String showEditArticle(
                @PathVariable Long id,
                @AuthenticationPrincipal UserDetails userDetails,
                Model model,
                RedirectAttributes redirectAttributes) {
            
            try {
                Article article = articleService.getArticleById(id);
                
                if (article == null) {
                    redirectAttributes.addFlashAttribute("error", "文章不存在");
                    return "redirect:/user/articles";
                }
                
                // 检查当前用户是否是文章作者
                User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
                    
                if (!article.getAuthor().getId().equals(user.getId())) {
                    redirectAttributes.addFlashAttribute("error", "您没有权限编辑此文章");
                    return "redirect:/user/articles";
                }
                
                // 确保将完整的文章对象添加到模型中
                model.addAttribute("article", article);
                return "article/editor";
                
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "加载文章失败: " + e.getMessage());
                return "redirect:/user/articles";
            }
        }

        // 新增编辑器页面路由
        @GetMapping("/editor")
        public String showEditorForm(
            @RequestParam(required = false) Long id,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
            
            // 如果提供了ID，加载现有文章
            if (id != null) {
                Article article = articleService.getArticleById(id);
                model.addAttribute("article", article);
            }
            return "article/editor";
        }
    }
    
    @GetMapping("/articles/{id}")
    public String getArticleDetail(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails,
        Model model) {
        
        // 获取文章并增加阅读量
        Article article = articleService.getArticleWithStats(id);
        model.addAttribute("article", article);
        
        // 检查当前用户是否是作者
        if (userDetails != null) {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
            model.addAttribute("isAuthor", article.getAuthor().getId().equals(currentUser.getId()));
        }
        
        return "article/detail";
    }

    @PostMapping("/api/articles/{id}/like")
    @ResponseBody
    public ResponseEntity<?> likeArticle(@PathVariable Long id) {
        articleService.incrementLikes(id);
        return ResponseEntity.ok().build();
    }
}


