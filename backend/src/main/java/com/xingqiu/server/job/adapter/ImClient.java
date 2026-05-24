package com.xingqiu.server.job.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.zip.Deflater;

/**
 * 腾讯云 IM 适配器（真实实现 — TLSSigAPIv2 UserSig 生成）
 */
@Service
public class ImClient {

    private static final Logger log = LoggerFactory.getLogger(ImClient.class);

    private final long sdkAppId;
    private final String key;

    public ImClient(
            @Value("${xingqiu.im.sdk-app-id:0}") long sdkAppId,
            @Value("${xingqiu.im.key:}") String key) {
        this.sdkAppId = sdkAppId;
        this.key = key;
        log.info("IM Client initialized — sdkAppId: {}", sdkAppId);
    }

    public Map<String, Object> generateToken(Long userId, String nickname, String avatarUrl) {
        String userIdStr = String.valueOf(userId);
        String userSig = genUserSig(userIdStr);
        log.info("IM token generated for userId={}", userId);
        return Map.of("token", userSig, "sdkAppId", String.valueOf(sdkAppId), "userId", userIdStr);
    }

    private String genUserSig(String userId) {
        long currTime = System.currentTimeMillis() / 1000;
        long expire = currTime + 86400 * 180;
        String identifier = base64UrlEncode(compress(userId.getBytes(StandardCharsets.UTF_8)));
        String sigDoc = "TLS.identifier:" + identifier + "\n"
                + "TLS.sdkappid:" + sdkAppId + "\n"
                + "TLS.time:" + currTime + "\n"
                + "TLS.expire:" + expire + "\n";
        String sig = base64UrlEncode(compress(hmacSha256(sigDoc, key).getBytes(StandardCharsets.UTF_8)));
        String json = "{\"TLS.ver\":\"2.0\","
                + "\"TLS.identifier\":\"" + identifier + "\","
                + "\"TLS.sdkappid\":" + sdkAppId + ","
                + "\"TLS.expire\":" + expire + ","
                + "\"TLS.time\":" + currTime + ","
                + "\"TLS.sig\":\"" + sig + "\"}";
        return base64UrlEncode(compress(json.getBytes(StandardCharsets.UTF_8)));
    }

    private String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC-SHA256 failed", e);
        }
    }

    private byte[] compress(byte[] data) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(data);
        deflater.finish();
        byte[] buf = new byte[2048];
        int len = deflater.deflate(buf);
        deflater.end();
        byte[] result = new byte[len];
        System.arraycopy(buf, 0, result, 0, len);
        return result;
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}
