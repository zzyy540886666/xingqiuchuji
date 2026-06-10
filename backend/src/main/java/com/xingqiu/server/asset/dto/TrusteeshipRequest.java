package com.xingqiu.server.asset.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;

/**
 * 创建托管请求
 */
public class TrusteeshipRequest {

    private Long assetId;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    /** 日租金，单位：分。未传时使用平台统一定价 */
    private Long dailyRateMinor;

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Long getDailyRateMinor() { return dailyRateMinor; }
    public void setDailyRateMinor(Long dailyRateMinor) { this.dailyRateMinor = dailyRateMinor; }
}
