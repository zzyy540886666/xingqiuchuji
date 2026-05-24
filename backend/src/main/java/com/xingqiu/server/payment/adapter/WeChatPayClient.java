package com.xingqiu.server.payment.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.payment.dto.WeChatPayParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WeChatPayClient {

    private static final Logger log = LoggerFactory.getLogger(WeChatPayClient.class);
    private static final String JSAPI_URL = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";

    private final RestClient restClient;
    private final String mchId;
    private final String appId;
    private final String apiV3Key;
    private final String notifyUrl;
    private final String merchantSerialNo;
    private final String privateKeyPath;
    private final String platformPublicKeyPath;
    private final ObjectMapper objectMapper;

    public WeChatPayClient(
            @Value("${xingqiu.wechat.pay.mch-id:}") String mchId,
            @Value("${xingqiu.wechat.mini-program.app-id:}") String appId,
            @Value("${xingqiu.wechat.pay.api-v3-key:}") String apiV3Key,
            @Value("${xingqiu.wechat.pay.notify-url:}") String notifyUrl,
            @Value("${xingqiu.wechat.pay.merchant-serial-no:}") String merchantSerialNo,
            @Value("${xingqiu.wechat.pay.private-key-path:}") String privateKeyPath,
            @Value("${xingqiu.wechat.pay.platform-public-key-path:}") String platformPublicKeyPath,
            ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(8));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
        this.mchId = mchId;
        this.appId = appId;
        this.apiV3Key = apiV3Key;
        this.notifyUrl = notifyUrl;
        this.merchantSerialNo = merchantSerialNo;
        this.privateKeyPath = privateKeyPath;
        this.platformPublicKeyPath = platformPublicKeyPath;
        this.objectMapper = objectMapper;
        log.info("WeChat Pay Client initialized");
    }

    public Map<String, Object> createJsapiOrder(String outTradeNo, Long amountMinor, String payerOpenid) {
        requireConfigured();
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("appid", appId);
            body.put("mchid", mchId);
            body.put("description", "星球出机-设备租赁");
            body.put("out_trade_no", outTradeNo);
            body.put("notify_url", notifyUrl);
            body.put("amount", Map.of("total", amountMinor, "currency", "CNY"));
            body.put("payer", Map.of("openid", payerOpenid));

            String bodyJson = objectMapper.writeValueAsString(body);
            String nonce = randomNonce();
            long timestamp = System.currentTimeMillis() / 1000;
            String signature = sign("POST\n/v3/pay/transactions/jsapi\n" + timestamp + "\n" + nonce + "\n" + bodyJson + "\n");
            String response = restClient.post().uri(JSAPI_URL)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", authorization(nonce, timestamp, signature))
                    .body(bodyJson).retrieve().body(String.class);
            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            String prepayId = (String) result.get("prepay_id");
            if (prepayId == null || prepayId.isBlank()) {
                throw new IllegalStateException("微信未返回 prepay_id");
            }
            return Map.of("prepayId", prepayId, "params", createPayParams(prepayId));
        } catch (Exception e) {
            log.error("WeChat JSAPI order creation failed: {}", e.getMessage(), e);
            throw new BizException(ErrorCode.PAY_SIGN_FAILED, "微信支付下单失败");
        }
    }

    public WeChatPayParams createPayParams(String prepayId) throws Exception {
        String nonce = randomNonce();
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String packageValue = "prepay_id=" + prepayId;
        String paySign = sign(appId + "\n" + timestamp + "\n" + nonce + "\n" + packageValue + "\n");
        WeChatPayParams params = new WeChatPayParams();
        params.setTimeStamp(timestamp);
        params.setNonceStr(nonce);
        params.setPackageVal(packageValue);
        params.setSignType("RSA");
        params.setPaySign(paySign);
        return params;
    }

    public boolean verifyNotify(String body, String signature, String timestamp, String nonce) {
        if (signature == null || timestamp == null || nonce == null) {
            return false;
        }
        try {
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(loadPlatformPublicKey());
            verifier.update((timestamp + "\n" + nonce + "\n" + body + "\n").getBytes(StandardCharsets.UTF_8));
            return verifier.verify(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            log.error("Callback signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> decryptNotify(String body) {
        try {
            Map<String, Object> resource = (Map<String, Object>) objectMapper.readValue(body, Map.class).get("resource");
            byte[] decrypted = aesGcmDecrypt(
                    Base64.getDecoder().decode((String) resource.get("ciphertext")),
                    apiV3Key.getBytes(StandardCharsets.UTF_8),
                    ((String) resource.get("nonce")).getBytes(StandardCharsets.UTF_8),
                    resource.get("associated_data") == null ? new byte[0] : ((String) resource.get("associated_data")).getBytes(StandardCharsets.UTF_8));
            return objectMapper.readValue(decrypted, Map.class);
        } catch (Exception e) {
            throw new BizException(ErrorCode.PAY_CALLBACK_VERIFY_FAIL, "支付回调解密失败");
        }
    }

    private void requireConfigured() {
        if (mchId.isBlank() || appId.isBlank() || apiV3Key.isBlank() || notifyUrl.isBlank()
                || merchantSerialNo.isBlank() || privateKeyPath.isBlank()) {
            throw new BizException(ErrorCode.PAY_SIGN_FAILED, "微信支付商户配置未完成");
        }
    }

    private String authorization(String nonce, long timestamp, String signature) {
        return "WECHATPAY2-SHA256-RSA2048 mchid=\"" + mchId + "\",nonce_str=\"" + nonce
                + "\",timestamp=\"" + timestamp + "\",serial_no=\"" + merchantSerialNo
                + "\",signature=\"" + signature + "\"";
    }

    private String sign(String content) throws Exception {
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(loadPrivateKey(privateKeyPath));
        signer.update(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signer.sign());
    }

    private PrivateKey loadPrivateKey(String path) throws Exception {
        String pem = readPem(path, "PRIVATE KEY");
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem)));
    }

    private PublicKey loadPlatformPublicKey() throws Exception {
        String pem = readPem(platformPublicKeyPath, "PUBLIC KEY");
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pem)));
    }

    private String readPem(String path, String type) throws Exception {
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("密钥文件未配置");
        }
        return Files.readString(Path.of(path), StandardCharsets.UTF_8)
                .replace("-----BEGIN " + type + "-----", "")
                .replace("-----END " + type + "-----", "")
                .replaceAll("\\s", "");
    }

    private String randomNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private byte[] aesGcmDecrypt(byte[] ciphertext, byte[] key, byte[] nonce, byte[] aad) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
        if (aad.length > 0) cipher.updateAAD(aad);
        return cipher.doFinal(ciphertext);
    }
}
