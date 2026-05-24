package com.xingqiu.server.distribution.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DistributionTeamResponse {

    private int level1Count;
    private int level2Count;
    private List<Member> members;

    public static class Member {
        private Long userId;
        private String nickname;
        private String avatarUrl;
        private Integer level;
        private LocalDateTime joinedAt;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public Integer getLevel() { return level; }
        public void setLevel(Integer level) { this.level = level; }
        public LocalDateTime getJoinedAt() { return joinedAt; }
        public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    }

    public int getLevel1Count() { return level1Count; }
    public void setLevel1Count(int level1Count) { this.level1Count = level1Count; }
    public int getLevel2Count() { return level2Count; }
    public void setLevel2Count(int level2Count) { this.level2Count = level2Count; }
    public List<Member> getMembers() { return members; }
    public void setMembers(List<Member> members) { this.members = members; }
}
