package com.xingqiu.server.repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.repair.domain.KnowledgeArticle;
import com.xingqiu.server.repair.dto.CreateArticleRequest;
import com.xingqiu.server.repair.mapper.KnowledgeArticleMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class KnowledgeService {

    private final KnowledgeArticleMapper articleMapper;

    public KnowledgeService(KnowledgeArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    public Page<KnowledgeArticle> listArticles(String category, boolean isAdmin, int page, int pageSize) {
        LambdaQueryWrapper<KnowledgeArticle> wrapper = new LambdaQueryWrapper<>();
        if (!isAdmin) {
            wrapper.eq(KnowledgeArticle::getStatus, "PUBLISHED");
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(KnowledgeArticle::getCategory, category);
        }
        wrapper.orderByDesc(KnowledgeArticle::getCreatedAt);
        return articleMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    public KnowledgeArticle createArticle(CreateArticleRequest request, Long authorId) {
        KnowledgeArticle article = new KnowledgeArticle();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCategory(request.getCategory());
        article.setTags(request.getTags());
        article.setAuthorId(authorId);
        article.setStatus("PUBLISHED");
        article.setViewCount(0);
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        articleMapper.insert(article);
        return article;
    }

    public KnowledgeArticle updateArticle(Long id, CreateArticleRequest request) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("知识文章不存在");
        }
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCategory(request.getCategory());
        article.setTags(request.getTags());
        article.setUpdatedAt(LocalDateTime.now());
        articleMapper.updateById(article);
        return article;
    }

    public KnowledgeArticle getArticle(Long id) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("知识文章不存在");
        }
        article.setViewCount(article.getViewCount() + 1);
        articleMapper.updateById(article);
        return article;
    }
}
