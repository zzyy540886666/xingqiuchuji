package com.xingqiu.server.payment.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.xingqiu.server.auth.mapper.UserMapper;
import com.xingqiu.server.order.domain.Order;
import com.xingqiu.server.order.domain.OrderStatus;
import com.xingqiu.server.order.service.OrderService;
import com.xingqiu.server.payment.adapter.WeChatPayClient;
import com.xingqiu.server.payment.domain.PaymentOrder;
import com.xingqiu.server.payment.domain.PaymentOrder.PaymentStatus;
import com.xingqiu.server.payment.mapper.PaymentOrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentOrderMapper paymentOrderMapper;
    @Mock
    private WeChatPayClient weChatPayClient;
    @Mock
    private OrderService orderService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PaymentSuccessOrchestrator paymentSuccessOrchestrator;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void handleCallbackClaimsPendingPaymentAndRunsSuccessOrchestration() {
        PaymentOrder payment = pendingPayment();
        Order order = order();

        callback(payment);
        when(paymentOrderMapper.update(isNull(), any(Wrapper.class))).thenReturn(1);
        when(orderService.getOrderEntity(20L)).thenReturn(order);

        paymentService.handleCallback("body", "sig", "ts", "nonce");

        verify(paymentOrderMapper).update(isNull(), any(Wrapper.class));
        verify(orderService).updateStatus(order, OrderStatus.PAID, "PAYMENT_CALLBACK");
        verify(paymentSuccessOrchestrator).orchestrate(order);
    }

    @Test
    void handleCallbackDoesNotRepeatWhenStatusClaimFails() {
        PaymentOrder payment = pendingPayment();

        callback(payment);
        when(paymentOrderMapper.update(isNull(), any(Wrapper.class))).thenReturn(0);

        paymentService.handleCallback("body", "sig", "ts", "nonce");

        verify(orderService, never()).updateStatus(any(), eq(OrderStatus.PAID), eq("PAYMENT_CALLBACK"));
        verify(paymentSuccessOrchestrator, never()).orchestrate(any());
    }

    private void callback(PaymentOrder payment) {
        when(weChatPayClient.verifyNotify("body", "sig", "ts", "nonce")).thenReturn(true);
        when(weChatPayClient.decryptNotify("body")).thenReturn(Map.of(
                "out_trade_no", "XQ20",
                "transaction_id", "wx-txn-1",
                "amount", Map.of("total", 490000)
        ));
        when(paymentOrderMapper.selectOne(any())).thenReturn(payment);
    }

    private PaymentOrder pendingPayment() {
        PaymentOrder payment = new PaymentOrder();
        payment.setId(10L);
        payment.setOrderId(20L);
        payment.setOutTradeNo("XQ20");
        payment.setAmountMinor(490000L);
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }

    private Order order() {
        Order order = new Order();
        order.setId(20L);
        order.setOrderNo("XQ202606010001");
        order.setUserId(30L);
        order.setStatus(OrderStatus.PENDING_PAY);
        order.setPayableMinor(490000L);
        return order;
    }
}
