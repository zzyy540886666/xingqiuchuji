package com.xingqiu.server.order.dto;

import java.time.LocalDate;

public class PreviewRequest {

    private Long skuId;

    private String orderType;

    private LocalDate rentStartDate;

    private LocalDate rentEndDate;

    /** Minimal address info for price estimation. */
    private String address;

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
}
