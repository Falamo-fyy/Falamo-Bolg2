package com.example.mytool.controller;

import com.example.mytool.entity.Article;
import com.example.mytool.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    /**
     * 首页控制器（嵌套类，处理模板渲染）
     * 负责展示文章列表页面
     */
    @Controller
    public class HomeController {

        /**
         * 获取首页文章列表
         * @param page 当前页码（从0开始）
         * @param size 每页显示数量
         * @param model 视图模型
         * @return 首页模板路径
         */
        @GetMapping("/")
        public String home(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
            
            // 获取分页文章数据（空关键词表示获取全部）
            Page<Article> articlePage = articleService.searchArticles("", PageRequest.of(page, size));
            
            // 设置视图模型属性
            model.addAttribute("articles", articlePage.getContent());  // 当前页文章列表
            model.addAttribute("currentPage", page);                   // 当前页码
            model.addAttribute("pageSize", size);                      // 每页数量
            model.addAttribute("totalPages", articlePage.getTotalPages()); // 总页数
            
            return "index";  // 对应templates/index.html
        }
    }

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
}