package com.xingqiu.server.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.auth.domain.User;
import com.xingqiu.server.auth.mapper.UserMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.community.adapter.TmsModerationClient;
import com.xingqiu.server.community.domain.*;
import com.xingqiu.server.community.dto.CommentResponse;
import com.xingqiu.server.community.dto.CreateCommentRequest;
import com.xingqiu.server.community.dto.CreatePostRequest;
import com.xingqiu.server.community.dto.PostResponse;
import com.xingqiu.server.community.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostMapper postMapper;
    private final PostMediaMapper postMediaMapper;
    private final PostAuditLogMapper postAuditLogMapper;
    private final CommentMapper commentMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostCollectMapper postCollectMapper;
    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;
    private final TmsModerationClient tmsModerationClient;

    public PostService(PostMapper postMapper,
                       PostMediaMapper postMediaMapper,
                       PostAuditLogMapper postAuditLogMapper,
                       CommentMapper commentMapper,
                       PostLikeMapper postLikeMapper,
                       PostCollectMapper postCollectMapper,
                       UserFollowMapper userFollowMapper,
                       UserMapper userMapper,
                       TmsModerationClient tmsModerationClient) {
        this.postMapper = postMapper;
        this.postMediaMapper = postMediaMapper;
        this.postAuditLogMapper = postAuditLogMapper;
        this.commentMapper = commentMapper;
        this.postLikeMapper = postLikeMapper;
        this.postCollectMapper = postCollectMapper;
        this.userFollowMapper = userFollowMapper;
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
     * currentUserId may be null for anonymous viewers.
     */
    public List<PostResponse> getFeed(String cursor, int limit, Long topicId, Long currentUserId) {
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
                .map(p -> toPostResponse(p, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * Get post detail: post + media + comments.
     */
    public PostResponse getPostDetail(Long postId, Long currentUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }
        assertVisible(post, currentUserId);

        PostResponse response = toPostResponse(post, currentUserId);

        // Load comments with author info
        LambdaQueryWrapper<Comment> commentQuery = new LambdaQueryWrapper<>();
        commentQuery.eq(Comment::getPostId, postId)
                .eq(Comment::getStatus, "ACTIVE")
                .orderByAsc(Comment::getCreatedAt);
        List<Comment> comments = commentMapper.selectList(commentQuery);
        response.setComments(comments.stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList()));

        return response;
    }

    // ---- like / collect / follow ----

    /**
     * Toggle like on a post. Idempotent: like if not liked, unlike if already liked.
     */
    @Transactional
    public PostResponse likePost(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }

        LambdaQueryWrapper<PostLike> query = new LambdaQueryWrapper<>();
        query.eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId);
        PostLike existing = postLikeMapper.selectOne(query);

        if (existing != null) {
            // Unlike
            postLikeMapper.deleteById(existing.getId());
            int newCount = Math.max(0, (post.getLikeCount() != null ? post.getLikeCount() : 0) - 1);
            post.setLikeCount(newCount);
            postMapper.updateById(post);
            log.info("User {} unliked post {}", userId, postId);
        } else {
            // Like
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            postLikeMapper.insert(like);
            int newCount = (post.getLikeCount() != null ? post.getLikeCount() : 0) + 1;
            post.setLikeCount(newCount);
            postMapper.updateById(post);
            log.info("User {} liked post {}", userId, postId);
        }

        return toPostResponse(post, userId);
    }

    /**
     * Toggle collect on a post. Idempotent.
     */
    @Transactional
    public PostResponse collectPost(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }

        LambdaQueryWrapper<PostCollect> query = new LambdaQueryWrapper<>();
        query.eq(PostCollect::getPostId, postId)
                .eq(PostCollect::getUserId, userId);
        PostCollect existing = postCollectMapper.selectOne(query);

        if (existing != null) {
            // Uncollect
            postCollectMapper.deleteById(existing.getId());
            int newCount = Math.max(0, (post.getCollectCount() != null ? post.getCollectCount() : 0) - 1);
            post.setCollectCount(newCount);
            postMapper.updateById(post);
            log.info("User {} uncollected post {}", userId, postId);
        } else {
            // Collect
            PostCollect collect = new PostCollect();
            collect.setPostId(postId);
            collect.setUserId(userId);
            postCollectMapper.insert(collect);
            int newCount = (post.getCollectCount() != null ? post.getCollectCount() : 0) + 1;
            post.setCollectCount(newCount);
            postMapper.updateById(post);
            log.info("User {} collected post {}", userId, postId);
        }

        return toPostResponse(post, userId);
    }

    /**
     * Toggle follow a user. Idempotent.
     */
    @Transactional
    public boolean followUser(Long followeeId, Long followerId) {
        if (followeeId.equals(followerId)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "不能关注自己");
        }

        // Verify followee exists
        User followee = userMapper.selectById(followeeId);
        if (followee == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        LambdaQueryWrapper<UserFollow> query = new LambdaQueryWrapper<>();
        query.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFolloweeId, followeeId);
        UserFollow existing = userFollowMapper.selectOne(query);

        if (existing != null) {
            // Unfollow
            userFollowMapper.deleteById(existing.getId());
            log.info("User {} unfollowed user {}", followerId, followeeId);
            return false;
        } else {
            // Follow
            UserFollow follow = new UserFollow();
            follow.setFollowerId(followerId);
            follow.setFolloweeId(followeeId);
            userFollowMapper.insert(follow);
            log.info("User {} followed user {}", followerId, followeeId);
            return true;
        }
    }

    // ---- comments ----

    /**
     * Create a comment on a post.
     */
    @Transactional
    public CommentResponse addComment(Long postId, Long userId, CreateCommentRequest request) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setStatus("ACTIVE");
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);

        // Update comment count
        post.setCommentCount((post.getCommentCount() != null ? post.getCommentCount() : 0) + 1);
        postMapper.updateById(post);

        log.info("Comment created: commentId={}, postId={}, userId={}", comment.getId(), postId, userId);

        return toCommentResponse(comment);
    }

    /**
     * Get paginated comments for a post.
     */
    public List<CommentResponse> getComments(Long postId, Long currentUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BizException(ErrorCode.POST_NOT_FOUND);
        }
        assertVisible(post, currentUserId);

        LambdaQueryWrapper<Comment> query = new LambdaQueryWrapper<>();
        query.eq(Comment::getPostId, postId)
                .eq(Comment::getStatus, "ACTIVE")
                .orderByAsc(Comment::getCreatedAt);
        List<Comment> comments = commentMapper.selectList(query);

        return comments.stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());
    }

    // ---- internal helpers ----

    private void assertVisible(Post post, Long currentUserId) {
        if (PostStatus.APPROVED.name().equals(post.getStatus())) {
            return;
        }
        if (currentUserId != null && currentUserId.equals(post.getUserId())) {
            return;
        }
        throw new BizException(ErrorCode.POST_NOT_FOUND);
    }

    private PostResponse toPostResponse(Post post, Long currentUserId) {
        PostResponse resp = new PostResponse();
        resp.setId(post.getId());
        resp.setUserId(post.getUserId());
        resp.setCircleId(post.getCircleId());
        resp.setTopicId(post.getTopicId());
        resp.setTitle(post.getTitle());
        resp.setContent(post.getContent());
        resp.setStatus(post.getStatus());
        resp.setLikeCount(post.getLikeCount());
        resp.setCollectCount(post.getCollectCount());
        resp.setCommentCount(post.getCommentCount());
        resp.setIsPinned(post.getIsPinned());
        resp.setCreatedAt(post.getCreatedAt());

        // Load author info
        User author = userMapper.selectById(post.getUserId());
        if (author != null) {
            resp.setAuthorNickname(author.getNickname());
            resp.setAuthorAvatar(author.getAvatarUrl());
        }

        // Check interaction state for current user
        if (currentUserId != null) {
            resp.setLiked(postLikeMapper.selectCount(
                    new LambdaQueryWrapper<PostLike>()
                            .eq(PostLike::getPostId, post.getId())
                            .eq(PostLike::getUserId, currentUserId)) > 0);
            resp.setCollected(postCollectMapper.selectCount(
                    new LambdaQueryWrapper<PostCollect>()
                            .eq(PostCollect::getPostId, post.getId())
                            .eq(PostCollect::getUserId, currentUserId)) > 0);
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

    private CommentResponse toCommentResponse(Comment comment) {
        CommentResponse resp = new CommentResponse();
        resp.setId(comment.getId());
        resp.setPostId(comment.getPostId());
        resp.setUserId(comment.getUserId());
        resp.setParentId(comment.getParentId());
        resp.setContent(comment.getContent());
        resp.setStatus(comment.getStatus());
        resp.setCreatedAt(comment.getCreatedAt());

        User author = userMapper.selectById(comment.getUserId());
        if (author != null) {
            resp.setAuthorNickname(author.getNickname());
            resp.setAuthorAvatar(author.getAvatarUrl());
        }

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
