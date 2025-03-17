package com.example.mytool.repository;

import com.example.mytool.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query("SELECT a FROM Article a WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Article> findByTitleContainingOrContentContaining(
        @Param("keyword") String titleKeyword,
        @Param("keyword") String contentKeyword,
        Pageable pageable);

    @Query("SELECT COUNT(a) FROM Article a WHERE a.author.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 根据作者ID查询文章
     * @param userId 作者ID
     * @param pageable 分页参数
     * @return 分页的文章列表
     */
    Page<Article> findByAuthorId(Long userId, Pageable pageable);
}
