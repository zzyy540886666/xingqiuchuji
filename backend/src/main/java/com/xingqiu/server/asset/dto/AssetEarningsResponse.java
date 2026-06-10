package com.xingqiu.server.asset.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AssetEarningsResponse {

    private Long totalRevenueMinor = 0L;
    private Long pendingRevenueMinor = 0L;
    private Long withdrawableRevenueMinor = 0L;
    private Long monthRevenueMinor = 0L;
    private Long trendTotalMinor = 0L;
    private Integer activeDeviceCount = 0;
    private LocalDateTime updatedAt;
    private List<TrendPoint> trend = new ArrayList<>();
    private List<CategorySummary> categories = new ArrayList<>();
    private List<DetailItem> details = new ArrayList<>();

    public Long getTotalRevenueMinor() { return totalRevenueMinor; }
    public void setTotalRevenueMinor(Long totalRevenueMinor) { this.totalRevenueMinor = totalRevenueMinor; }

    public Long getPendingRevenueMinor() { return pendingRevenueMinor; }
    public void setPendingRevenueMinor(Long pendingRevenueMinor) { this.pendingRevenueMinor = pendingRevenueMinor; }

    public Long getWithdrawableRevenueMinor() { return withdrawableRevenueMinor; }
    public void setWithdrawableRevenueMinor(Long withdrawableRevenueMinor) { this.withdrawableRevenueMinor = withdrawableRevenueMinor; }

    public Long getMonthRevenueMinor() { return monthRevenueMinor; }
    public void setMonthRevenueMinor(Long monthRevenueMinor) { this.monthRevenueMinor = monthRevenueMinor; }

    public Long getTrendTotalMinor() { return trendTotalMinor; }
    public void setTrendTotalMinor(Long trendTotalMinor) { this.trendTotalMinor = trendTotalMinor; }

    public Integer getActiveDeviceCount() { return activeDeviceCount; }
    public void setActiveDeviceCount(Integer activeDeviceCount) { this.activeDeviceCount = activeDeviceCount; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<TrendPoint> getTrend() { return trend; }
    public void setTrend(List<TrendPoint> trend) { this.trend = trend; }

    public List<CategorySummary> getCategories() { return categories; }
    public void setCategories(List<CategorySummary> categories) { this.categories = categories; }

    public List<DetailItem> getDetails() { return details; }
    public void setDetails(List<DetailItem> details) { this.details = details; }

    public static class TrendPoint {
        private String date;
        private Long amountMinor = 0L;

        public TrendPoint() {}

        public TrendPoint(String date, Long amountMinor) {
            this.date = date;
            this.amountMinor = amountMinor;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public Long getAmountMinor() { return amountMinor; }
        public void setAmountMinor(Long amountMinor) { this.amountMinor = amountMinor; }
    }

    public static class CategorySummary {
        private String type;
        private String name;
        private Long amountMinor = 0L;

        public CategorySummary() {}

        public CategorySummary(String type, String name, Long amountMinor) {
            this.type = type;
            this.name = name;
            this.amountMinor = amountMinor;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Long getAmountMinor() { return amountMinor; }
        public void setAmountMinor(Long amountMinor) { this.amountMinor = amountMinor; }
    }

    public static class DetailItem {
        private Long id;
        private Long assetId;
        private String assetName;
        private String assetImageUrl;
        private String type;
        private Long amountMinor = 0L;
        private String status;
        private LocalDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getAssetId() { return assetId; }
        public void setAssetId(Long assetId) { this.assetId = assetId; }

        public String getAssetName() { return assetName; }
        public void setAssetName(String assetName) { this.assetName = assetName; }

        public String getAssetImageUrl() { return assetImageUrl; }
        public void setAssetImageUrl(String assetImageUrl) { this.assetImageUrl = assetImageUrl; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Long getAmountMinor() { return amountMinor; }
        public void setAmountMinor(Long amountMinor) { this.amountMinor = amountMinor; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
