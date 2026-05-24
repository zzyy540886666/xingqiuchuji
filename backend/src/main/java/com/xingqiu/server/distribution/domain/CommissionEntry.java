package com.xingqiu.server.distribution.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("commission_entries")
public class CommissionEntry {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long orderId;
    private Long beneficiaryUserId;
    private Long sourceUserId;
    private Integer level;
    private Long amountMinor;
    private Integer ratePercent;
    private String status;
    private LocalDateTime protectUntil;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getBeneficiaryUserId() { return beneficiaryUserId; }
    public void setBeneficiaryUserId(Long beneficiaryUserId) { this.beneficiaryUserId = beneficiaryUserId; }
    public Long getSourceUserId() { return sourceUserId; }
    public void setSourceUserId(Long sourceUserId) { this.sourceUserId = sourceUserId; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Long getAmountMinor() { return amountMinor; }
    public void setAmountMinor(Long amountMinor) { this.amountMinor = amountMinor; }
    public Integer getRatePercent() { return ratePercent; }
    public void setRatePercent(Integer ratePercent) { this.ratePercent = ratePercent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getProtectUntil() { return protectUntil; }
    public void setProtectUntil(LocalDateTime protectUntil) { this.protectUntil = protectUntil; }
    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
