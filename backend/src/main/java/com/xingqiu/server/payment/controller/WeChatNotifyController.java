package com.xingqiu.server.payment.controller;

import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/pay")
public class WeChatNotifyController {

    private static final Logger log = LoggerFactory.getLogger(WeChatNotifyController.class);

    private final PaymentService paymentService;

    public WeChatNotifyController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/wechat/notify")
    public ApiResponse<Void> handleWeChatNotify(
            @RequestBody String body,
            @RequestHeader(value = "Wechatpay-Signature", required = false) String signature,
            @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce) {
        log.info("Received WeChat payment callback");
        paymentService.handleCallback(body, signature, timestamp, nonce);
        return ApiResponse.ok(null);
    }
}
