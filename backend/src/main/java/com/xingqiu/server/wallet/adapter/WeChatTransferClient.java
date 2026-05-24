package com.xingqiu.server.wallet.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingqiu.server.auth.domain.User;
import com.xingqiu.server.auth.mapper.UserMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.wallet.domain.WithdrawRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.*;

/**
 * 微信支付商家转账到零钱适配器（真实实现）
 */
@Service
public class WeChatTransferClient {

    private static final Logger log = LoggerFactory.getLogger(WeChatTransferClient.class);
    private static final String TRANSFER_URL = "https://api.mch.weixin.qq.com/v3/transfer/batches";

    private final RestClient restClient;
    private final String mchId;
    private final String appId;
    private final String merchantSerialNo;
    private final String privateKeyPath;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;

    public WeChatTransferClient(
            @Value("${xingqiu.wechat.pay.mch-id}") String mchId,
            @Value("${xingqiu.wechat.mini-program.app-id}") String appId,
            @Value("${xingqiu.wechat.pay.merchant-serial-no:}") String merchantSerialNo,
            @Value("${xingqiu.wechat.pay.private-key-path:}") String privateKeyPath,
            ObjectMapper objectMapper,
            UserMapper userMapper) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(8));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
        this.mchId = mchId;
        this.appId = appId;
        this.merchantSerialNo = merchantSerialNo;
        this.privateKeyPath = privateKeyPath;
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
        log.info("WeChat Transfer Client initialized");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> transferToWallet(WithdrawRequest withdrawRequest) {
        try {
            requireConfigured();
            String outBatchNo = "XQ" + System.currentTimeMillis();
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("appid", appId);
            body.put("out_batch_no", outBatchNo);
            body.put("batch_name", "星球出机提现");
            body.put("batch_remark", "用户提现");
            body.put("total_amount", withdrawRequest.getAmountMinor());
            body.put("total_num", 1);

            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("out_detail_no", withdrawRequest.getIdempotencyKey());
            detail.put("transfer_amount", withdrawRequest.getAmountMinor());
            detail.put("transfer_remark", "提现到零钱");
            detail.put("openid", getUserOpenid(withdrawRequest.getUserId()));
            body.put("transfer_detail_list", List.of(detail));

            String bodyJson = objectMapper.writeValueAsString(body);
            String nonceStr = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
            long timestamp = System.currentTimeMillis() / 1000;
            String signature = signRequest("POST", "/v3/transfer/batches", timestamp, nonceStr, bodyJson);

            String response = restClient.post()
                    .uri(TRANSFER_URL)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Wechatpay-Serial", merchantSerialNo)
                    .header("Authorization", buildAuthHeader(nonceStr, timestamp, signature))
                    .body(bodyJson)
                    .retrieve()
                    .body(String.class);

            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            String batchId = (String) result.get("batch_id");
            if (batchId == null || batchId.isBlank()) {
                throw new IllegalStateException("微信未返回批次单号");
            }

            log.info("WeChat transfer initiated — batchId: {}, amount: {} 分", batchId, withdrawRequest.getAmountMinor());
            return Map.of("batch_id", batchId, "status", "PROCESSING");

        } catch (Exception e) {
            log.error("WeChat transfer failed: {}", e.getMessage(), e);
            throw new RuntimeException("提现转账失败: " + e.getMessage(), e);
        }
    }

    private String signRequest(String method, String path, long timestamp, String nonce, String body) throws Exception {
        String data = method + "\n" + path + "\n" + timestamp + "\n" + nonce + "\n" + body + "\n";
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(loadMerchantPrivateKey());
        sig.update(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(sig.sign());
    }

    private String buildAuthHeader(String nonce, long timestamp, String signature) {
        return "WECHATPAY2-SHA256-RSA2048 mchid=\"" + mchId
                + "\",nonce_str=\"" + nonce + "\",timestamp=\"" + timestamp
                + "\",serial_no=\"" + merchantSerialNo + "\",signature=\"" + signature + "\"";
    }

    private void requireConfigured() {
        if (mchId == null || mchId.isBlank() || appId == null || appId.isBlank()
                || merchantSerialNo == null || merchantSerialNo.isBlank()
                || privateKeyPath == null || privateKeyPath.isBlank()) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "微信商家转账配置未完成");
        }
    }

    private String getUserOpenid(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getOpenid() == null || user.getOpenid().isBlank()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "当前用户未绑定微信身份");
        }
        return user.getOpenid();
    }

    private PrivateKey loadMerchantPrivateKey() throws Exception {
        String pem = Files.readString(Path.of(privateKeyPath), StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(pem);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }
}
