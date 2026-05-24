package com.xingqiu.server.distribution.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("invite_relations")
public class InviteRelation {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long inviterUserId;
    private Long inviteeUserId;
    private Integer level;
    private LocalDateTime boundAt;
    private String source;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInviterUserId() { return inviterUserId; }
    public void setInviterUserId(Long inviterUserId) { this.inviterUserId = inviterUserId; }
    public Long getInviteeUserId() { return inviteeUserId; }
    public void setInviteeUserId(Long inviteeUserId) { this.inviteeUserId = inviteeUserId; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public LocalDateTime getBoundAt() { return boundAt; }
    public void setBoundAt(LocalDateTime boundAt) { this.boundAt = boundAt; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
