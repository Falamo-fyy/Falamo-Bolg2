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

import java.util.Date;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

import com.example.mytool.dto.ArticleDto;

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

    /**
     * 更新文章
     * @param id 文章ID
     * @param updatedArticle 更新后的文章内容
     * @return 更新后的文章
     */
    @Transactional
    public Article updateArticle(Long id, Article updatedArticle) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("文章不存在"));
        
        // 更新文章内容
        article.setTitle(updatedArticle.getTitle());
        article.setContent(updatedArticle.getContent());
        article.setCategory(updatedArticle.getCategory());
        article.setUpdatedAt(new Date());
        
        return articleRepository.save(article);
    }

    /**
     * 删除文章
     * @param id 文章ID
     */
    @CacheEvict(key = "#id")
    @Transactional
    public void deleteArticle(Long id) {
        // 检查文章是否存在
        if (!articleRepository.existsById(id)) {
            throw new EntityNotFoundException("文章不存在");
        }
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

    @Cacheable(key = "#id")
    public ArticleDto getArticleDtoById(Long id) {
        Article article = articleRepository.findById(id).orElse(null);
        return ArticleDto.fromEntity(article);
    }

    public Page<ArticleDto> getUserArticlesDtos(Long userId, Pageable pageable) {
        Page<Article> articles = articleRepository.findByAuthorId(userId, pageable);
        return articles.map(ArticleDto::fromEntity);
    }

    /**
     * 获取用户的文章列表
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 分页的文章列表
     */
    public Page<Article> getUserArticles(Long userId, Pageable pageable) {
        return articleRepository.findByAuthorId(userId, pageable);
    }

}
