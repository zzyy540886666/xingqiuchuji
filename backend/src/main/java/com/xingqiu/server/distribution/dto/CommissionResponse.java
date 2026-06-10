package com.xingqiu.server.distribution.dto;

import java.time.LocalDateTime;

public class CommissionResponse {

    private Long id;
    private Long orderId;
    private String orderNo;
    private Long beneficiaryUserId;
    private Long sourceUserId;
    private Integer level;
    private Long amountMinor;
    private Integer ratePercent;
    private String status;
    private String productImage;
    private String productName;
    private String sourceType;
    private String settleTime;
    private LocalDateTime protectUntil;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
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
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSettleTime() { return settleTime; }
    public void setSettleTime(String settleTime) { this.settleTime = settleTime; }
    public LocalDateTime getProtectUntil() { return protectUntil; }
    public void setProtectUntil(LocalDateTime protectUntil) { this.protectUntil = protectUntil; }
    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
