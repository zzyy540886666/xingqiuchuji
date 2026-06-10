package com.xingqiu.server.order.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class OrderResponse {

    private String id;
    private String orderNo;
    private Long userId;
    private Long skuId;
    private String orderType;
    private String status;
    private Long amountMinor;
    private Long depositMinor;
    private Long shippingMinor;
    private Long discountMinor;
    private Long payableMinor;
    private LocalDate rentStartDate;
    private LocalDate rentEndDate;
    private String addressJson;
    private String idempotencyKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getAmountMinor() { return amountMinor; }
    public void setAmountMinor(Long amountMinor) { this.amountMinor = amountMinor; }

    public Long getDepositMinor() { return depositMinor; }
    public void setDepositMinor(Long depositMinor) { this.depositMinor = depositMinor; }

    public Long getShippingMinor() { return shippingMinor; }
    public void setShippingMinor(Long shippingMinor) { this.shippingMinor = shippingMinor; }

    public Long getDiscountMinor() { return discountMinor; }
    public void setDiscountMinor(Long discountMinor) { this.discountMinor = discountMinor; }

    public Long getPayableMinor() { return payableMinor; }
    public void setPayableMinor(Long payableMinor) { this.payableMinor = payableMinor; }

    public LocalDate getRentStartDate() { return rentStartDate; }
    public void setRentStartDate(LocalDate rentStartDate) { this.rentStartDate = rentStartDate; }

    public LocalDate getRentEndDate() { return rentEndDate; }
    public void setRentEndDate(LocalDate rentEndDate) { this.rentEndDate = rentEndDate; }

    public String getAddressJson() { return addressJson; }
    public void setAddressJson(String addressJson) { this.addressJson = addressJson; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
