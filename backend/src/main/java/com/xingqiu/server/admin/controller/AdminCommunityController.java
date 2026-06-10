package com.xingqiu.server.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.auth.domain.User;
import com.xingqiu.server.auth.mapper.UserMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.community.domain.Circle;
import com.xingqiu.server.community.domain.Comment;
import com.xingqiu.server.community.domain.Post;
import com.xingqiu.server.community.domain.PostAuditLog;
import com.xingqiu.server.community.domain.PostStatus;
import com.xingqiu.server.community.mapper.CircleMapper;
import com.xingqiu.server.community.mapper.CommentMapper;
import com.xingqiu.server.community.mapper.PostAuditLogMapper;
import com.xingqiu.server.community.mapper.PostMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/community")
public class AdminCommunityController {

    private final PostMapper postMapper;
    private final CircleMapper circleMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final PostAuditLogMapper postAuditLogMapper;

    public AdminCommunityController(PostMapper postMapper,
                                    CircleMapper circleMapper,
                                    CommentMapper commentMapper,
                                    UserMapper userMapper,
                                    PostAuditLogMapper postAuditLogMapper) {
        this.postMapper = postMapper;
        this.circleMapper = circleMapper;
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.postAuditLogMapper = postAuditLogMapper;
    }

    @GetMapping("/posts")
    public ApiResponse<PageResult<Map<String, Object>>> listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long circleId,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Post> wrapper = Wrappers.lambdaQuery(Post.class);
        if (status != null && !status.isBlank()) {
            wrapper.eq(Post::getStatus, status);
        }
        if (circleId != null) {
            wrapper.eq(Post::getCircleId, circleId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(q -> q.like(Post::getTitle, keyword).or().like(Post::getContent, keyword));
        }
        wrapper.orderByDesc(Post::getIsPinned).orderByDesc(Post::getCreatedAt);

        Page<Post> result = postMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<Long, User> users = usersOf(result.getRecords());
        Map<Long, Circle> circles = circlesOf(result.getRecords());
        List<Map<String, Object>> rows = result.getRecords().stream()
                .map(post -> postRow(post, users.get(post.getUserId()), circles.get(post.getCircleId())))
                .toList();
        return ApiResponse.ok(PageResult.of(rows, result.getTotal(), page, pageSize));
    }

    @PostMapping("/posts/{id}/revoke")
    @Transactional
    public ApiResponse<Void> revokePost(@PathVariable Long id) {
        Post post = requirePost(id);
        post.setStatus(PostStatus.REVOKED.name());
        post.setIsPinned(false);
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.updateById(post);
        writeAuditLog(id, "人工处理", "已撤销", "后台撤销帖子");
        return ApiResponse.ok(null);
    }

    @PostMapping("/posts/{id}/restore")
    @Transactional
    public ApiResponse<Void> restorePost(@PathVariable Long id) {
        Post post = requirePost(id);
        post.setStatus(PostStatus.APPROVED.name());
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.updateById(post);
        writeAuditLog(id, "人工处理", "已恢复", "后台恢复帖子");
        return ApiResponse.ok(null);
    }

    @PostMapping("/posts/{id}/pin")
    public ApiResponse<Void> pinPost(@PathVariable Long id) {
        updatePinned(id, true);
        return ApiResponse.ok(null);
    }

    @PostMapping("/posts/{id}/unpin")
    public ApiResponse<Void> unpinPost(@PathVariable Long id) {
        updatePinned(id, false);
        return ApiResponse.ok(null);
    }

    @GetMapping("/topics")
    public ApiResponse<PageResult<Map<String, Object>>> listTopics(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Circle> wrapper = Wrappers.lambdaQuery(Circle.class);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(q -> q.like(Circle::getName, keyword).or().like(Circle::getDescription, keyword));
        }
        wrapper.orderByDesc(Circle::getMemberCount);

        Page<Circle> result = circleMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<Map<String, Object>> rows = result.getRecords().stream().map(this::topicRow).toList();
        return ApiResponse.ok(PageResult.of(rows, result.getTotal(), page, pageSize));
    }

    @PostMapping("/topics/{id}/revoke")
    @Transactional
    public ApiResponse<Void> revokeTopic(@PathVariable Long id) {
        requireCircle(id);
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getCircleId, id)
                .ne(Post::getStatus, PostStatus.REVOKED.name())
                .set(Post::getStatus, PostStatus.REVOKED.name())
                .set(Post::getIsPinned, false)
                .set(Post::getUpdatedAt, LocalDateTime.now()));
        return ApiResponse.ok(null);
    }

    @PostMapping("/topics/{id}/restore")
    @Transactional
    public ApiResponse<Void> restoreTopic(@PathVariable Long id) {
        requireCircle(id);
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getCircleId, id)
                .eq(Post::getStatus, PostStatus.REVOKED.name())
                .set(Post::getStatus, PostStatus.APPROVED.name())
                .set(Post::getUpdatedAt, LocalDateTime.now()));
        return ApiResponse.ok(null);
    }

    @GetMapping("/analytics")
    public ApiResponse<Map<String, Object>> analytics() {
        long postTotal = postMapper.selectCount(Wrappers.lambdaQuery(Post.class));
        long approvedTotal = postMapper.selectCount(Wrappers.lambdaQuery(Post.class)
                .eq(Post::getStatus, PostStatus.APPROVED.name()));
        long auditingTotal = postMapper.selectCount(Wrappers.lambdaQuery(Post.class)
                .in(Post::getStatus, PostStatus.AUDITING.name(), PostStatus.MANUAL_REVIEW.name()));
        long revokedTotal = postMapper.selectCount(Wrappers.lambdaQuery(Post.class)
                .eq(Post::getStatus, PostStatus.REVOKED.name()));
        long topicTotal = circleMapper.selectCount(Wrappers.lambdaQuery(Circle.class));
        long commentTotal = commentMapper.selectCount(Wrappers.lambdaQuery(Comment.class));

        List<Post> hotPosts = postMapper.selectPage(new Page<>(1, 5), Wrappers.lambdaQuery(Post.class)
                .eq(Post::getStatus, PostStatus.APPROVED.name())
                .orderByDesc(Post::getLikeCount)).getRecords();
        Map<Long, User> users = usersOf(hotPosts);
        Map<Long, Circle> circles = circlesOf(hotPosts);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("summary", List.of(
                metric("帖子总数", postTotal),
                metric("已发布", approvedTotal),
                metric("待处理", auditingTotal),
                metric("已撤销", revokedTotal),
                metric("话题数", topicTotal),
                metric("评论数", commentTotal)
        ));
        data.put("statusSplit", List.of(
                metric("已发布", approvedTotal),
                metric("待处理", auditingTotal),
                metric("已撤销", revokedTotal),
                metric("已拒绝", postMapper.selectCount(Wrappers.lambdaQuery(Post.class)
                        .eq(Post::getStatus, PostStatus.REJECTED.name())))
        ));
        data.put("hotPosts", hotPosts.stream()
                .map(post -> postRow(post, users.get(post.getUserId()), circles.get(post.getCircleId())))
                .toList());
        data.put("trend", buildTrend());
        return ApiResponse.ok(data);
    }

    private List<Map<String, Object>> buildTrend() {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atStartOfDay();
            long count = postMapper.selectCount(Wrappers.lambdaQuery(Post.class)
                    .ge(Post::getCreatedAt, start)
                    .lt(Post::getCreatedAt, end));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", day.toString());
            row.put("count", count);
            trend.add(row);
        }
        return trend;
    }

    private Map<String, Object> postRow(Post post, User user, Circle circle) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", post.getId());
        row.put("title", post.getTitle());
        row.put("content", post.getContent());
        row.put("status", post.getStatus());
        row.put("statusText", postStatusText(post.getStatus()));
        row.put("authorName", user == null ? "未知用户" : user.getNickname());
        row.put("circleId", post.getCircleId());
        row.put("circleName", circle == null ? "未分组" : circle.getName());
        row.put("likeCount", post.getLikeCount() == null ? 0 : post.getLikeCount());
        row.put("commentCount", post.getCommentCount() == null ? 0 : post.getCommentCount());
        row.put("pinned", Boolean.TRUE.equals(post.getIsPinned()));
        row.put("createdAt", post.getCreatedAt());
        row.put("updatedAt", post.getUpdatedAt());
        return row;
    }

    private Map<String, Object> topicRow(Circle circle) {
        long approvedCount = postMapper.selectCount(Wrappers.lambdaQuery(Post.class)
                .eq(Post::getCircleId, circle.getId())
                .eq(Post::getStatus, PostStatus.APPROVED.name()));
        long totalCount = postMapper.selectCount(Wrappers.lambdaQuery(Post.class)
                .eq(Post::getCircleId, circle.getId()));
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", circle.getId());
        row.put("name", circle.getName());
        row.put("description", circle.getDescription());
        row.put("memberCount", circle.getMemberCount() == null ? 0 : circle.getMemberCount());
        row.put("postCount", totalCount);
        row.put("activePostCount", approvedCount);
        row.put("status", totalCount > 0 && approvedCount == 0 ? "REVOKED" : "ACTIVE");
        row.put("createdAt", circle.getCreatedAt());
        return row;
    }

    private Map<String, Object> metric(String label, Object value) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("label", label);
        row.put("value", value);
        return row;
    }

    private Map<Long, User> usersOf(List<Post> posts) {
        Set<Long> ids = posts.stream().map(Post::getUserId).filter(id -> id != null).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private Map<Long, Circle> circlesOf(List<Post> posts) {
        Set<Long> ids = posts.stream().map(Post::getCircleId).filter(id -> id != null).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return circleMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(Circle::getId, Function.identity()));
    }

    private Post requirePost(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }
        return post;
    }

    private Circle requireCircle(Long id) {
        Circle circle = circleMapper.selectById(id);
        if (circle == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "话题不存在");
        }
        return circle;
    }

    private void updatePinned(Long id, boolean pinned) {
        Post post = requirePost(id);
        post.setIsPinned(pinned);
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.updateById(post);
    }

    private void writeAuditLog(Long postId, String source, String result, String remark) {
        PostAuditLog log = new PostAuditLog();
        log.setPostId(postId);
        log.setAuditSource(source);
        log.setResult(result);
        log.setRawResponse(remark);
        log.setCreatedAt(LocalDateTime.now());
        postAuditLogMapper.insert(log);
    }

    private String postStatusText(String status) {
        if (PostStatus.APPROVED.name().equals(status)) {
            return "已发布";
        }
        if (PostStatus.REVOKED.name().equals(status)) {
            return "已撤销";
        }
        if (PostStatus.REJECTED.name().equals(status)) {
            return "已拒绝";
        }
        if (PostStatus.MANUAL_REVIEW.name().equals(status)) {
            return "人工复核";
        }
        return "待审核";
    }
}
