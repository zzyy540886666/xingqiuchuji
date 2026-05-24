package com.xingqiu.server.order.domain;

public enum OrderStatus {

    PENDING_PAY,
    PAID,
    CANCELLED,
    FULFILLING,
    COMPLETED,
    REFUNDING,
    REFUNDED
}
