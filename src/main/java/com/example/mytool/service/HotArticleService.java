package com.example.mytool.service;

import com.example.mytool.dto.ArticleDto;
import com.example.mytool.entity.Article;
import com.example.mytool.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class HotArticleService {

    private static final String HOT_ARTICLES_KEY = "hot:articles";
    private static final int HOT_ARTICLES_COUNT = 10;
    private static final long CACHE_TTL_HOURS = 24;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取热门文章列表（前10篇点赞最多的文章）
     * 优先从Redis缓存中获取，如果缓存不存在则从数据库查询并缓存
     */
    public List<ArticleDto> getHotArticles() {
        // 尝试从缓存获取
        Set<ZSetOperations.TypedTuple<Object>> cachedArticles = 
                redisTemplate.opsForZSet().reverseRangeWithScores(HOT_ARTICLES_KEY, 0, HOT_ARTICLES_COUNT - 1);
        
        // 如果缓存存在且不为空，直接返回缓存数据
        if (cachedArticles != null && !cachedArticles.isEmpty()) {
            List<ArticleDto> result = new ArrayList<>();
            for (ZSetOperations.TypedTuple<Object> tuple : cachedArticles) {
                Long articleId = Long.valueOf(tuple.getValue().toString());
                Double score = tuple.getScore(); // 点赞数
                
                // 从Redis获取文章详情
                ArticleDto articleDto = (ArticleDto) redisTemplate.opsForValue().get("article:dto:" + articleId);
                if (articleDto != null) {
                    // 确保点赞数是最新的
                    articleDto.setLikes(score.intValue());
                    result.add(articleDto);
                }
            }
            return result;
        }
        
        // 缓存不存在，从数据库查询并缓存
        return refreshHotArticlesCache();
    }

    /**
     * 刷新热门文章缓存
     * 每小时自动执行一次
     */
    @Transactional
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public List<ArticleDto> refreshHotArticlesCache() {
        // 从数据库查询点赞数最多的10篇文章
        List<Article> hotArticles = articleRepository.findAll(
                PageRequest.of(0, HOT_ARTICLES_COUNT, Sort.by(Sort.Direction.DESC, "likes"))
        ).getContent();
        
        // 清除旧缓存
        redisTemplate.delete(HOT_ARTICLES_KEY);
        
        // 将热门文章添加到有序集合中，以点赞数为分数
        for (Article article : hotArticles) {
            // 将文章ID添加到有序集合，以点赞数为分数
            redisTemplate.opsForZSet().add(HOT_ARTICLES_KEY, article.getId().toString(), article.getLikes());
            
            // 缓存文章DTO对象
            ArticleDto articleDto = ArticleDto.fromEntity(article);
            redisTemplate.opsForValue().set("article:dto:" + article.getId(), articleDto, CACHE_TTL_HOURS, TimeUnit.HOURS);
        }
        
        // 设置缓存过期时间
        redisTemplate.expire(HOT_ARTICLES_KEY, CACHE_TTL_HOURS, TimeUnit.HOURS);
        
        // 转换为DTO并返回
        return hotArticles.stream()
                .map(ArticleDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 当文章点赞数更新时，更新缓存中的排序
     */
    public void updateArticleScore(Long articleId, int likes) {
        // 更新有序集合中的分数
        redisTemplate.opsForZSet().add(HOT_ARTICLES_KEY, articleId.toString(), likes);
        
        // 获取缓存中的文章DTO
        ArticleDto articleDto = (ArticleDto) redisTemplate.opsForValue().get("article:dto:" + articleId);
        if (articleDto != null) {
            // 更新点赞数
            articleDto.setLikes(likes);
            // 更新缓存
            redisTemplate.opsForValue().set("article:dto:" + articleId, articleDto, CACHE_TTL_HOURS, TimeUnit.HOURS);
        }
    }
}