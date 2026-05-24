package com.xingqiu.server.community.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("post_audit_logs")
public class PostAuditLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long postId;
    private String auditSource;
    private String result;
    private String rawResponse;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public String getAuditSource() { return auditSource; }
    public void setAuditSource(String auditSource) { this.auditSource = auditSource; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
