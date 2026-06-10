package com.xingqiu.server.distribution.dto;

public class DistributionInfoResponse {

    private Overview overview;
    private String inviteCode;
    private String inviteUrl;
    private String invitePath;
    private TeamStats teamStats;

    public static class Overview {
        private long pendingCommission;
        private long withdrawableCommission;
        private long withdrawnCommission;

        public long getPendingCommission() { return pendingCommission; }
        public void setPendingCommission(long pendingCommission) { this.pendingCommission = pendingCommission; }
        public long getWithdrawableCommission() { return withdrawableCommission; }
        public void setWithdrawableCommission(long withdrawableCommission) { this.withdrawableCommission = withdrawableCommission; }
        public long getWithdrawnCommission() { return withdrawnCommission; }
        public void setWithdrawnCommission(long withdrawnCommission) { this.withdrawnCommission = withdrawnCommission; }
    }

    public static class TeamStats {
        private int level1Count;
        private int level2Count;
        private int todayNew;
        private int totalInvite;

        public int getLevel1Count() { return level1Count; }
        public void setLevel1Count(int level1Count) { this.level1Count = level1Count; }
        public int getLevel2Count() { return level2Count; }
        public void setLevel2Count(int level2Count) { this.level2Count = level2Count; }
        public int getTodayNew() { return todayNew; }
        public void setTodayNew(int todayNew) { this.todayNew = todayNew; }
        public int getTotalInvite() { return totalInvite; }
        public void setTotalInvite(int totalInvite) { this.totalInvite = totalInvite; }
    }

    public Overview getOverview() { return overview; }
    public void setOverview(Overview overview) { this.overview = overview; }
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public String getInviteUrl() { return inviteUrl; }
    public void setInviteUrl(String inviteUrl) { this.inviteUrl = inviteUrl; }
    public String getInvitePath() { return invitePath; }
    public void setInvitePath(String invitePath) { this.invitePath = invitePath; }
    public TeamStats getTeamStats() { return teamStats; }
    public void setTeamStats(TeamStats teamStats) { this.teamStats = teamStats; }
}
