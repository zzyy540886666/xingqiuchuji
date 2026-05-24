package com.xingqiu.server.asset.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("trusteeship_slots")
public class TrusteeshipSlot {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long assetId;

    private Long userId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 状态: ACTIVE / COMPLETED / CANCELLED */
    private String status;

    /** 日租金，单位：分 */
    private Long dailyRateMinor;

    /** 累计收益，单位：分 */
    private Long totalRevenueMinor;

    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getDailyRateMinor() { return dailyRateMinor; }
    public void setDailyRateMinor(Long dailyRateMinor) { this.dailyRateMinor = dailyRateMinor; }

    public Long getTotalRevenueMinor() { return totalRevenueMinor; }
    public void setTotalRevenueMinor(Long totalRevenueMinor) { this.totalRevenueMinor = totalRevenueMinor; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
