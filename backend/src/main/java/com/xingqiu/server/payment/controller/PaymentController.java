package com.xingqiu.server.payment.controller;

import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.payment.dto.WeChatPayParams;
import com.xingqiu.server.payment.service.PaymentService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{orderId}/payments/wechat-jsapi")
    public ApiResponse<WeChatPayParams> createWeChatPayment(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();
        WeChatPayParams params = paymentService.createPayment(userId, orderId);
        return ApiResponse.ok(params);
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new IllegalStateException("No authenticated user found");
    }
}
