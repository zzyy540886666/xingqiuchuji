package com.xingqiu.server.distribution.dto;

public class DistributionInfoResponse {

    private String inviteCode;
    private String inviteUrl;
    private TeamStats teamStats;

    public static class TeamStats {
        private int level1Count;
        private int level2Count;

        public int getLevel1Count() { return level1Count; }
        public void setLevel1Count(int level1Count) { this.level1Count = level1Count; }
        public int getLevel2Count() { return level2Count; }
        public void setLevel2Count(int level2Count) { this.level2Count = level2Count; }
    }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public String getInviteUrl() { return inviteUrl; }
    public void setInviteUrl(String inviteUrl) { this.inviteUrl = inviteUrl; }
    public TeamStats getTeamStats() { return teamStats; }
    public void setTeamStats(TeamStats teamStats) { this.teamStats = teamStats; }
}
