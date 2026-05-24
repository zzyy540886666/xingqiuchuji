package com.xingqiu.server.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.auth.domain.User;
import com.xingqiu.server.auth.mapper.UserMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.order.domain.Order;
import com.xingqiu.server.order.domain.OrderStatus;
import com.xingqiu.server.order.service.OrderService;
import com.xingqiu.server.payment.adapter.WeChatPayClient;
import com.xingqiu.server.payment.domain.PaymentOrder;
import com.xingqiu.server.payment.domain.PaymentOrder.PaymentStatus;
import com.xingqiu.server.payment.dto.WeChatPayParams;
import com.xingqiu.server.payment.mapper.PaymentOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentOrderMapper paymentOrderMapper;
    private final WeChatPayClient weChatPayClient;
    private final OrderService orderService;
    private final UserMapper userMapper;

    public PaymentService(PaymentOrderMapper paymentOrderMapper,
                          WeChatPayClient weChatPayClient,
                          OrderService orderService,
                          UserMapper userMapper) {
        this.paymentOrderMapper = paymentOrderMapper;
        this.weChatPayClient = weChatPayClient;
        this.orderService = orderService;
        this.userMapper = userMapper;
    }

    @Transactional
    public WeChatPayParams createPayment(Long userId, Long orderId) {
        // 1. Get order and validate ownership
        Order order = orderService.getOrderEntity(orderId);

        if (!order.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权为此订单发起支付");
        }

        if (order.getStatus() != OrderStatus.PENDING_PAY) {
            throw new BizException(ErrorCode.ORDER_NOT_PAYABLE,
                    "订单状态为 " + order.getStatus() + "，不可支付");
        }

        // 2. Check if payment already exists (idempotent)
        PaymentOrder existing = findPendingByOrderId(orderId);
        if (existing != null) {
            log.info("Payment already exists for order {}, returning existing prepay data", orderId);
            return buildWeChatPayParams(existing);
        }

        // 3. Generate outTradeNo
        String outTradeNo = "XQ" + orderId + System.currentTimeMillis();

        // 4. Create payment order
        PaymentOrder payment = new PaymentOrder();
        payment.setOrderId(orderId);
        payment.setOutTradeNo(outTradeNo);
        payment.setAmountMinor(order.getPayableMinor());
        payment.setChannel("WECHAT_JSAPI");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        // 5. Call WeChat Pay API
        String payerOpenid = getUserOpenid(userId);
        Map<String, Object> wxResult = weChatPayClient.createJsapiOrder(outTradeNo, order.getPayableMinor(), payerOpenid);

        WeChatPayParams params = (WeChatPayParams) wxResult.get("params");
        payment.setPrepayId((String) wxResult.get("prepayId"));
        payment.setPaySign(params.getPaySign());

        paymentOrderMapper.insert(payment);

        log.info("Payment created: outTradeNo={}, orderId={}", outTradeNo, orderId);

        // 6. Build return params for mini program
        return params;
    }

    @Transactional
    public void handleCallback(String body, String signature, String timestamp, String nonce) {
        // 1. Verify WeChat signature
        if (!weChatPayClient.verifyNotify(body, signature, timestamp, nonce)) {
            throw new BizException(ErrorCode.PAY_CALLBACK_VERIFY_FAIL);
        }

        // 2. Decrypt notify data
        Map<String, Object> notifyData = weChatPayClient.decryptNotify(body);
        String outTradeNo = (String) notifyData.get("out_trade_no");
        String transactionId = (String) notifyData.get("transaction_id");

        // 3. Find payment order by outTradeNo
        PaymentOrder payment = findByOutTradeNo(outTradeNo);
        if (payment == null) {
            log.warn("Payment not found for outTradeNo: {}", outTradeNo);
            throw new BizException(ErrorCode.NOT_FOUND, "支付单不存在: " + outTradeNo);
        }

        // 4. Validate callback amount matches payment order
        Object callbackAmount = notifyData.get("amount");
        if (callbackAmount != null) {
            Long cbAmount;
            if (callbackAmount instanceof Map<?, ?> amountMap) {
                Object total = amountMap.get("total");
                cbAmount = total instanceof Number ? ((Number) total).longValue() : Long.parseLong(total.toString());
            } else {
                cbAmount = callbackAmount instanceof Number
                        ? ((Number) callbackAmount).longValue()
                        : Long.parseLong(callbackAmount.toString());
            }
            if (!cbAmount.equals(payment.getAmountMinor())) {
                log.error("Callback amount mismatch: expected={}, got={}", payment.getAmountMinor(), cbAmount);
                throw new BizException(ErrorCode.PAY_CALLBACK_VERIFY_FAIL, "回调金额与支付单不一致");
            }
        }

        // 5. Idempotency check — if already SUCCESS, return ok
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("Payment {} already SUCCESS, idempotent return", outTradeNo);
            return;
        }

        // 6. Update payment status
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentOrderMapper.updateById(payment);

        // 6. Update order status to PAID (updateStatus handles transition + persistence)
        Order order = orderService.getOrderEntity(payment.getOrderId());
        orderService.updateStatus(order, OrderStatus.PAID, "PAYMENT_CALLBACK");

        log.info("Payment callback processed: outTradeNo={}, txnId={}", outTradeNo, transactionId);
    }

    private PaymentOrder findPendingByOrderId(Long orderId) {
        LambdaQueryWrapper<PaymentOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentOrder::getOrderId, orderId);
        wrapper.eq(PaymentOrder::getStatus, PaymentStatus.PENDING);
        return paymentOrderMapper.selectOne(wrapper);
    }

    private PaymentOrder findByOutTradeNo(String outTradeNo) {
        LambdaQueryWrapper<PaymentOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentOrder::getOutTradeNo, outTradeNo);
        return paymentOrderMapper.selectOne(wrapper);
    }

    private WeChatPayParams buildWeChatPayParams(PaymentOrder payment) {
        try {
            return weChatPayClient.createPayParams(payment.getPrepayId());
        } catch (Exception e) {
            throw new BizException(ErrorCode.PAY_SIGN_FAILED, "微信支付签名失败");
        }
    }

    private String getUserOpenid(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getOpenid() == null || user.getOpenid().isBlank()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "当前用户未绑定微信身份");
        }
        return user.getOpenid();
    }
}
