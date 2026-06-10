package com.xingqiu.server.community.controller;

import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.community.adapter.StsAdapter;
import com.xingqiu.server.community.domain.Circle;
import com.xingqiu.server.community.dto.CommentResponse;
import com.xingqiu.server.community.dto.CreateCommentRequest;
import com.xingqiu.server.community.dto.CreatePostRequest;
import com.xingqiu.server.community.dto.PostResponse;
import com.xingqiu.server.community.service.CircleService;
import com.xingqiu.server.community.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/community")
@Validated
public class CommunityController {

    private final PostService postService;
    private final CircleService circleService;
    private final StsAdapter stsAdapter;

    public CommunityController(PostService postService,
                               CircleService circleService,
                               StsAdapter stsAdapter) {
        this.postService = postService;
        this.circleService = circleService;
        this.stsAdapter = stsAdapter;
    }

    /**
     * Create a new post.
     */
    @PostMapping("/posts")
    public ApiResponse<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        Long userId = currentUserId();
        PostResponse response = postService.createPost(userId, request);
        return ApiResponse.ok(response);
    }

    /**
     * Get community feed (cursor-based pagination).
     */
    @GetMapping("/feed")
    public ApiResponse<List<PostResponse>> getFeed(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit,
            @RequestParam(required = false) Long topicId) {
        Long currentUserId = currentUserIdOrNull();
        List<PostResponse> feed = postService.getFeed(cursor, limit, topicId, currentUserId);
        return ApiResponse.ok(feed);
    }

    /**
     * Get post detail.
     */
    @GetMapping("/posts/{postId}")
    public ApiResponse<PostResponse> getPostDetail(@PathVariable Long postId) {
        Long currentUserId = currentUserIdOrNull();
        PostResponse response = postService.getPostDetail(postId, currentUserId);
        return ApiResponse.ok(response);
    }

    /**
     * Toggle like on a post.
     */
    @PostMapping("/posts/{postId}/like")
    public ApiResponse<PostResponse> likePost(@PathVariable Long postId) {
        Long userId = currentUserId();
        PostResponse response = postService.likePost(postId, userId);
        return ApiResponse.ok(response);
    }

    /**
     * Toggle collect on a post.
     */
    @PostMapping("/posts/{postId}/collect")
    public ApiResponse<PostResponse> collectPost(@PathVariable Long postId) {
        Long userId = currentUserId();
        PostResponse response = postService.collectPost(postId, userId);
        return ApiResponse.ok(response);
    }

    /**
     * Toggle follow on a user.
     */
    @PostMapping("/users/{userId}/follow")
    public ApiResponse<Boolean> followUser(@PathVariable Long userId) {
        Long followerId = currentUserId();
        boolean following = postService.followUser(userId, followerId);
        return ApiResponse.ok(following);
    }

    /**
     * Create a comment on a post.
     */
    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<CommentResponse> addComment(@PathVariable Long postId,
                                                    @Valid @RequestBody CreateCommentRequest request) {
        Long userId = currentUserId();
        CommentResponse comment = postService.addComment(postId, userId, request);
        return ApiResponse.ok(comment);
    }

    /**
     * Get comments for a post.
     */
    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<List<CommentResponse>> getComments(@PathVariable Long postId) {
        Long currentUserId = currentUserIdOrNull();
        List<CommentResponse> comments = postService.getComments(postId, currentUserId);
        return ApiResponse.ok(comments);
    }

    /**
     * Get STS temporary credential for COS upload.
     */
    @PostMapping("/upload/sts")
    public ApiResponse<Map<String, Object>> getStsCredential() {
        Map<String, Object> credential = stsAdapter.getTempCredential();
        return ApiResponse.ok(credential);
    }

    /**
     * List all circles.
     */
    @GetMapping("/circles")
    public ApiResponse<List<Circle>> listCircles() {
        List<Circle> circles = circleService.listCircles();
        return ApiResponse.ok(circles);
    }

    /**
     * Join a circle.
     */
    @PostMapping("/circles/{id}/join")
    public ApiResponse<Void> joinCircle(@PathVariable("id") Long circleId) {
        Long userId = currentUserId();
        circleService.joinCircle(userId, circleId);
        return ApiResponse.ok(null);
    }

    /**
     * Get circle feed.
     */
    @GetMapping("/circles/{id}/feed")
    public ApiResponse<List<PostResponse>> getCircleFeed(
            @PathVariable("id") Long circleId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
        List<PostResponse> feed = circleService.getCircleFeed(circleId, cursor, limit);
        return ApiResponse.ok(feed);
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Long currentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        return principal instanceof Long userId ? userId : null;
    }
}
