package com.example.mytool.service;

import com.example.mytool.dto.CommentDto;
import com.example.mytool.entity.Article;
import com.example.mytool.entity.Comment;
import com.example.mytool.entity.User;
import com.example.mytool.repository.ArticleRepository;
import com.example.mytool.repository.CommentRepository;
import com.example.mytool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, ArticleRepository articleRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * 获取文章的所有评论
     */
    public List<CommentDto> getArticleComments(Long articleId) {
        List<Comment> comments = commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId);
        return comments.stream()
                .map(CommentDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 添加评论
     */
    @Transactional
    public CommentDto addComment(Long articleId, String username, String content) {
        // 获取用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        // 获取文章
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("文章不存在"));
        
        // 创建评论
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setArticle(article);
        comment.setAuthor(user);
        
        // 保存评论
        Comment savedComment = commentRepository.save(comment);
        
        return CommentDto.fromEntity(savedComment);
    }
    
    /**
     * 删除评论
     */
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("评论不存在"));
        
        // 检查是否是评论作者
        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new SecurityException("只有评论作者才能删除评论");
        }
        
        commentRepository.delete(comment);
    }
}