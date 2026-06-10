package com.xingqiu.server.auth.dto;

import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    @Size(max = 32, message = "昵称最多32个字符")
    private String nickname;

    @Size(max = 512, message = "头像URL过长")
    private String avatarUrl;

    public UpdateUserRequest() {}

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
