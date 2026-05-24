package com.xingqiu.server.repair.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.repair.domain.KnowledgeArticle;
import com.xingqiu.server.repair.dto.CreateArticleRequest;
import com.xingqiu.server.repair.service.KnowledgeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/knowledge/articles")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @GetMapping
    public ApiResponse<PageResult<KnowledgeArticle>> listArticles(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Page<KnowledgeArticle> result = knowledgeService.listArticles(category, isAdmin, page, pageSize);
        return ApiResponse.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @PostMapping
    public ApiResponse<KnowledgeArticle> createArticle(@RequestBody CreateArticleRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        requireAdminOrTechnician(auth);
        Long authorId = (Long) auth.getPrincipal();
        KnowledgeArticle article = knowledgeService.createArticle(request, authorId);
        return ApiResponse.ok(article);
    }

    @PatchMapping("/{id}")
    public ApiResponse<KnowledgeArticle> updateArticle(@PathVariable Long id,
                                                        @RequestBody CreateArticleRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        requireAdminOrTechnician(auth);
        KnowledgeArticle article = knowledgeService.updateArticle(id, request);
        return ApiResponse.ok(article);
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeArticle> getArticle(@PathVariable Long id) {
        KnowledgeArticle article = knowledgeService.getArticle(id);
        return ApiResponse.ok(article);
    }

    private void requireAdminOrTechnician(Authentication auth) {
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
                && !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TECHNICIAN"))) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
    }
}
