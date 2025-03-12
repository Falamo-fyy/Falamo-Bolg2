package com.example.mytool.service;

import com.example.mytool.entity.Article;
import com.example.mytool.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;


@CacheConfig(cacheNames = "articles")
@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    @Cacheable(key = "#id")
    public Article getArticleById(Long id) {
        return articleRepository.findById(id).orElse(null);
    }

    @CacheEvict(allEntries = true)
    public Article createArticle(Article article) {
        return articleRepository.save(article);
    }

    public Article updateArticle(Long id, Article article) {
        // 实现更新逻辑
        return article;
    }

    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }


    // 新增分页查询方法
    public Page<Article> searchArticles(String keyword, Pageable pageable) {
        return articleRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
    }

    // 新增阅读量递增方法
    @Transactional
    public void incrementViews(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("文章不存在"));
        article.setViews(article.getViews() + 1);
        articleRepository.save(article);
    }

    public Page<Article> getUserArticles(Long userId, Pageable pageable) {
        return articleRepository.findByAuthorId(userId, pageable);
    }

}
