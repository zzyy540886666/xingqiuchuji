package com.xingqiu.server.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Tencent Cloud API TC3-HMAC-SHA256 签名工具
 */
public final class TencentCloudSigner {

    private static final String ALGORITHM = "TC3-HMAC-SHA256";

    private TencentCloudSigner() {}

    public static String sign(String secretId, String secretKey,
                               String service, String host,
                               String action, String version, String region,
                               String payload) {
        try {
            SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");
            dateFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat timeFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            timeFmt.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date now = new Date();
            String date = dateFmt.format(now);
            String timestamp = timeFmt.format(now);

            // Step 1: CanonicalRequest
            String httpMethod = "POST";
            String canonicalUri = "/";
            String canonicalQuery = "";
            String contentType = "application/json; charset=utf-8";
            String signedHeaders = "content-type;host";
            String hashedPayload = sha256Hex(payload);
            String canonicalRequest = httpMethod + "\n" + canonicalUri + "\n" + canonicalQuery + "\n"
                    + "content-type:" + contentType + "\n" + "host:" + host + "\n" + "\n"
                    + signedHeaders + "\n" + hashedPayload;

            // Step 2: StringToSign
            String credentialScope = date + "/" + service + "/tc3_request";
            String hashedCanonical = sha256Hex(canonicalRequest);
            String stringToSign = ALGORITHM + "\n" + timestamp + "\n" + credentialScope + "\n" + hashedCanonical;

            // Step 3: Signature
            byte[] secretDate = hmacSha256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
            byte[] secretService = hmacSha256(secretDate, service);
            byte[] secretSigning = hmacSha256(secretService, "tc3_request");
            String signature = bytesToHex(hmacSha256(secretSigning, stringToSign));

            // Step 4: Authorization header
            return ALGORITHM + " Credential=" + secretId + "/" + credentialScope
                    + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;

        } catch (Exception e) {
            throw new RuntimeException("Tencent Cloud API signing failed", e);
        }
    }

    private static byte[] hmacSha256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256Hex(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return bytesToHex(md.digest(data.getBytes(StandardCharsets.UTF_8)));
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
