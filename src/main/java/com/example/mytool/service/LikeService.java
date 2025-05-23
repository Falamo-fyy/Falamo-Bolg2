package com.example.mytool.service;

import com.example.mytool.entity.Article;
import com.example.mytool.entity.ArticleLike;
import com.example.mytool.entity.User;
import com.example.mytool.repository.ArticleLikeRepository;
import com.example.mytool.repository.ArticleRepository;
import com.example.mytool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
public class LikeService {

    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Autowired
    public LikeService(ArticleLikeRepository articleLikeRepository, ArticleRepository articleRepository, UserRepository userRepository) {
        this.articleLikeRepository = articleLikeRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * 用户点赞文章
     */
    @Transactional
    public boolean likeArticle(String username, Long articleId) {
        // 获取用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        // 获取文章
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("文章不存在"));
        
        // 检查用户是否已点赞
        if (articleLikeRepository.existsByArticleIdAndUserId(articleId, user.getId())) {
            return false; // 已点赞，不能重复点赞
        }
        
        // 创建点赞记录
        ArticleLike like = new ArticleLike(article, user);
        articleLikeRepository.save(like);
        
        // 更新文章点赞数
        long likeCount = articleLikeRepository.countByArticleId(articleId);
        article.setLikes((int) likeCount);
        articleRepository.save(article);
        
        return true;
    }
    
    /**
     * 检查用户是否已点赞文章
     */
    public boolean hasUserLikedArticle(String username, Long articleId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        return articleLikeRepository.existsByArticleIdAndUserId(articleId, user.getId());
    }
    
    /**
     * 取消点赞
     */
    @Transactional
    public boolean unlikeArticle(String username, Long articleId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        // 删除点赞记录
        articleLikeRepository.deleteByArticleIdAndUserId(articleId, user.getId());
        
        // 更新文章点赞数
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("文章不存在"));
        
        long likeCount = articleLikeRepository.countByArticleId(articleId);
        article.setLikes((int) likeCount);
        articleRepository.save(article);
        
        return true;
    }
}