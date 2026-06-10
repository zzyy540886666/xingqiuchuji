package com.xingqiu.server.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateOrderRequest {

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @NotBlank(message = "订单类型不能为空")
    @Size(max = 32, message = "订单类型最长32字符")
    private String orderType;

    private LocalDate rentStartDate;

    private LocalDate rentEndDate;

    @Size(max = 500, message = "地址信息最长500字符")
    private String address;

    private Long couponDiscountMinor;

    private Long lightYearDiscountMinor;

    @Size(max = 128, message = "幂等键最长128字符")
    private String idempotencyKey;

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public LocalDate getRentStartDate() { return rentStartDate; }
    public void setRentStartDate(LocalDate rentStartDate) { this.rentStartDate = rentStartDate; }

    public LocalDate getRentEndDate() { return rentEndDate; }
    public void setRentEndDate(LocalDate rentEndDate) { this.rentEndDate = rentEndDate; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Long getCouponDiscountMinor() { return couponDiscountMinor; }
    public void setCouponDiscountMinor(Long couponDiscountMinor) { this.couponDiscountMinor = couponDiscountMinor; }

    public Long getLightYearDiscountMinor() { return lightYearDiscountMinor; }
    public void setLightYearDiscountMinor(Long lightYearDiscountMinor) { this.lightYearDiscountMinor = lightYearDiscountMinor; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
