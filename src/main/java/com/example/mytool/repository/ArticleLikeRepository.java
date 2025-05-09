package com.example.mytool.repository;

import com.example.mytool.entity.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
    
    // 检查用户是否已点赞
    boolean existsByArticleIdAndUserId(Long articleId, Long userId);
    
    // 获取文章点赞数
    @Query("SELECT COUNT(al) FROM ArticleLike al WHERE al.article.id = ?1")
    long countByArticleId(Long articleId);
    
    // 删除点赞记录（取消点赞）
    void deleteByArticleIdAndUserId(Long articleId, Long userId);
}