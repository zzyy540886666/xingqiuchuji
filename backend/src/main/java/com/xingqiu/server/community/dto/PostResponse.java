package com.xingqiu.server.community.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponse {

    private Long id;
    private Long userId;
    private Long circleId;
    private Long topicId;
    private String title;
    private String content;
    private String status;
    private Integer likeCount;
    private Integer collectCount;
    private Integer commentCount;
    private Boolean isPinned;
    private Boolean liked;
    private Boolean collected;
    private LocalDateTime createdAt;

    private String authorNickname;
    private String authorAvatar;

    private List<MediaItem> mediaList;
    private List<CommentResponse> comments;

    public static class MediaItem {
        private Long id;
        private String url;
        private String type;
        private Integer sortOrder;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCircleId() { return circleId; }
    public void setCircleId(Long circleId) { this.circleId = circleId; }
    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getCollectCount() { return collectCount; }
    public void setCollectCount(Integer collectCount) { this.collectCount = collectCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public Boolean getIsPinned() { return isPinned; }
    public void setIsPinned(Boolean isPinned) { this.isPinned = isPinned; }
    public Boolean getLiked() { return liked; }
    public void setLiked(Boolean liked) { this.liked = liked; }
    public Boolean getCollected() { return collected; }
    public void setCollected(Boolean collected) { this.collected = collected; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getAuthorNickname() { return authorNickname; }
    public void setAuthorNickname(String authorNickname) { this.authorNickname = authorNickname; }
    public String getAuthorAvatar() { return authorAvatar; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }
    public List<MediaItem> getMediaList() { return mediaList; }
    public void setMediaList(List<MediaItem> mediaList) { this.mediaList = mediaList; }
    public List<CommentResponse> getComments() { return comments; }
    public void setComments(List<CommentResponse> comments) { this.comments = comments; }
}
