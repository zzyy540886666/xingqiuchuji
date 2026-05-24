package com.xingqiu.server.auth.dto;

public class UserSummary {

    private Long id;

    private String nickname;

    private String avatarUrl;

    private int membershipLevel;

    private boolean isNativeResident;

    public UserSummary() {
        this.membershipLevel = 0;
        this.isNativeResident = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public int getMembershipLevel() { return membershipLevel; }
    public void setMembershipLevel(int membershipLevel) { this.membershipLevel = membershipLevel; }

    public boolean getIsNativeResident() { return isNativeResident; }
    public void setIsNativeResident(boolean isNativeResident) { this.isNativeResident = isNativeResident; }
}
