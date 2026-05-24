package com.xingqiu.server.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.community.domain.Post;
import com.xingqiu.server.community.domain.PostAuditLog;
import com.xingqiu.server.community.domain.PostStatus;
import com.xingqiu.server.community.mapper.PostAuditLogMapper;
import com.xingqiu.server.community.mapper.PostMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminPostService {

    private final PostMapper postMapper;
    private final PostAuditLogMapper postAuditLogMapper;

    public AdminPostService(PostMapper postMapper, PostAuditLogMapper postAuditLogMapper) {
        this.postMapper = postMapper;
        this.postAuditLogMapper = postAuditLogMapper;
    }

    public PageResult<Post> listPosts(String status, int page, int pageSize) {
        LambdaQueryWrapper<Post> query = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            query.eq(Post::getStatus, status.toUpperCase());
        } else {
            query.in(Post::getStatus, PostStatus.AUDITING.name(), PostStatus.MANUAL_REVIEW.name());
        }
        query.orderByDesc(Post::getCreatedAt);

        Page<Post> pageObj = new Page<>(page, pageSize);
        Page<Post> result = postMapper.selectPage(pageObj, query);
        return PageResult.of(result.getRecords(), result.getTotal(), page, pageSize);
    }

    public void approvePost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BizException(ErrorCode.POST_NOT_FOUND);
        post.setStatus(PostStatus.APPROVED.name());
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.updateById(post);

        logAudit(postId, "APPROVED", "管理员人审通过");
    }

    public void rejectPost(Long postId, String reason) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BizException(ErrorCode.POST_NOT_FOUND);
        post.setStatus(PostStatus.REJECTED.name());
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.updateById(post);

        logAudit(postId, "REJECTED", reason != null ? reason : "管理员人审驳回");
    }

    private void logAudit(Long postId, String result, String rawResponse) {
        PostAuditLog auditLog = new PostAuditLog();
        auditLog.setPostId(postId);
        auditLog.setAuditSource("MANUAL");
        auditLog.setResult(result);
        auditLog.setRawResponse(rawResponse);
        auditLog.setCreatedAt(LocalDateTime.now());
        postAuditLogMapper.insert(auditLog);
    }
}
