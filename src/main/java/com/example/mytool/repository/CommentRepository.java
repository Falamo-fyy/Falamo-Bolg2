package com.example.mytool.repository;

import com.example.mytool.entity.Article;
import com.example.mytool.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.article = :article")
    int deleteAllByArticle(@Param("article") Article article);
    List<Comment> findByArticleIdOrderByCreatedAtDesc(Long articleId);
    long countByArticleId(Long articleId);
}