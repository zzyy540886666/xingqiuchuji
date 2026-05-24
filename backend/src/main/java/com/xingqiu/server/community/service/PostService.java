package com.xingqiu.server.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.auth.domain.User;
import com.xingqiu.server.auth.mapper.UserMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.community.adapter.TmsModerationClient;
import com.xingqiu.server.community.domain.*;
import com.xingqiu.server.community.dto.CreatePostRequest;
import com.xingqiu.server.community.dto.PostResponse;
import com.xingqiu.server.community.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostMapper postMapper;
    private final PostMediaMapper postMediaMapper;
    private final PostAuditLogMapper postAuditLogMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final TmsModerationClient tmsModerationClient;

    public PostService(PostMapper postMapper,
                       PostMediaMapper postMediaMapper,
                       PostAuditLogMapper postAuditLogMapper,
                       CommentMapper commentMapper,
                       UserMapper userMapper,
                       TmsModerationClient tmsModerationClient) {
        this.postMapper = postMapper;
        this.postMediaMapper = postMediaMapper;
        this.postAuditLogMapper = postAuditLogMapper;
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.tmsModerationClient = tmsModerationClient;
    }

    /**
     * Create a post: save as AUDITING, save media, async TMS moderation.
     */
    @Transactional
    public PostResponse createPost(Long userId, CreatePostRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Post post = new Post();
        post.setUserId(userId);
        post.setCircleId(request.getCircleId());
        post.setTopicId(request.getTopicId());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setStatus(PostStatus.AUDITING.name());
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        postMapper.insert(post);

        // Save media
        if (request.getMediaUrls() != null && !request.getMediaUrls().isEmpty()) {
            List<PostMedia> mediaList = new ArrayList<>();
            for (int i = 0; i < request.getMediaUrls().size(); i++) {
                String url = request.getMediaUrls().get(i);
                PostMedia media = new PostMedia();
                media.setPostId(post.getId());
                media.setUrl(url);
                media.setType(detectMediaType(url));
                media.setSortOrder(i);
                mediaList.add(media);
            }
            for (PostMedia m : mediaList) {
                postMediaMapper.insert(m);
            }
        }

        // Async TMS moderation
        tmsModerationClient.moderateAndUpdate(post.getId(), request.getContent());

        log.info("Post created: postId={}, userId={}", post.getId(), userId);

        return toPostResponse(post, userId);
    }

    /**
     * Get feed: cursor-based pagination of approved posts.
     */
    public List<PostResponse> getFeed(String cursor, int limit, Long topicId) {
        LambdaQueryWrapper<Post> query = new LambdaQueryWrapper<>();
        query.eq(Post::getStatus, PostStatus.APPROVED.name());

        if (topicId != null) {
            query.eq(Post::getTopicId, topicId);
        }
        if (cursor != null && !cursor.isEmpty()) {
            try {
                LocalDateTime cursorTime = LocalDateTime.parse(cursor);
                query.lt(Post::getCreatedAt, cursorTime);
            } catch (Exception e) {
                log.warn("Invalid cursor format: {}", cursor);
            }
        }
        query.orderByDesc(Post::getCreatedAt);

        int safeLimit = Math.max(1, Math.min(limit, 50));
        Page<Post> page = new Page<>(1, safeLimit);
        Page<Post> result = postMapper.selectPage(page, query);

        return result.getRecords().stream()
                .map(p -> toPostResponse(p, p.getUserId()))
                .collect(Collectors.toList());
    }

    /**
     * Get post detail: post + media + comments.
     */
    public PostResponse getPostDetail(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }

        PostResponse response = toPostResponse(post, post.getUserId());

        // Load comments
        LambdaQueryWrapper<Comment> commentQuery = new LambdaQueryWrapper<>();
        commentQuery.eq(Comment::getPostId, postId)
                .eq(Comment::getStatus, "ACTIVE")
                .orderByAsc(Comment::getCreatedAt);
        List<Comment> comments = commentMapper.selectList(commentQuery);
        // Comments are loaded but kept simple for now
        // In a full implementation, they'd be nested in the response

        return response;
    }

    // ---- internal helpers ----

    private PostResponse toPostResponse(Post post, Long userId) {
        PostResponse resp = new PostResponse();
        resp.setId(post.getId());
        resp.setUserId(post.getUserId());
        resp.setCircleId(post.getCircleId());
        resp.setTopicId(post.getTopicId());
        resp.setTitle(post.getTitle());
        resp.setContent(post.getContent());
        resp.setStatus(post.getStatus());
        resp.setLikeCount(post.getLikeCount());
        resp.setCommentCount(post.getCommentCount());
        resp.setIsPinned(post.getIsPinned());
        resp.setCreatedAt(post.getCreatedAt());

        // Load author info
        User author = userMapper.selectById(post.getUserId());
        if (author != null) {
            resp.setAuthorNickname(author.getNickname());
            resp.setAuthorAvatar(author.getAvatarUrl());
        }

        // Load media
        LambdaQueryWrapper<PostMedia> mediaQuery = new LambdaQueryWrapper<>();
        mediaQuery.eq(PostMedia::getPostId, post.getId())
                .orderByAsc(PostMedia::getSortOrder);
        List<PostMedia> mediaList = postMediaMapper.selectList(mediaQuery);
        resp.setMediaList(mediaList.stream().map(m -> {
            PostResponse.MediaItem item = new PostResponse.MediaItem();
            item.setId(m.getId());
            item.setUrl(m.getUrl());
            item.setType(m.getType());
            item.setSortOrder(m.getSortOrder());
            return item;
        }).collect(Collectors.toList()));

        return resp;
    }

    private String detectMediaType(String url) {
        if (url == null) return "IMAGE";
        String lower = url.toLowerCase();
        if (lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".avi")) {
            return "VIDEO";
        }
        return "IMAGE";
    }
}
