package com.xingqiu.server.community.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingqiu.server.common.util.TencentCloudSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * 腾讯云 STS 临时凭证适配器（真实实现 — TC3-HMAC-SHA256 签名）
 */
@Service
public class StsAdapter {

    private static final Logger log = LoggerFactory.getLogger(StsAdapter.class);
    private static final String HOST = "sts.tencentcloudapi.com";
    private static final String SERVICE = "sts";
    private static final String VERSION = "2018-08-13";
    private static final String ACTION = "GetFederationToken";

    private final RestClient restClient;
    private final String secretId;
    private final String secretKey;
    private final String region;
    private final String bucket;
    private final int durationSeconds;
    private final ObjectMapper objectMapper;

    public StsAdapter(
            @Value("${xingqiu.cos.secret-id}") String secretId,
            @Value("${xingqiu.cos.secret-key}") String secretKey,
            @Value("${xingqiu.cos.region:ap-guangzhou}") String region,
            @Value("${xingqiu.cos.bucket}") String bucket,
            @Value("${xingqiu.cos.sts-duration:1800}") int durationSeconds,
            ObjectMapper objectMapper) {
        this.restClient = RestClient.create();
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.region = region;
        this.bucket = bucket;
        this.durationSeconds = durationSeconds;
        this.objectMapper = objectMapper;
        log.info("STS Adapter initialized — region: {}, bucket: {}", region, bucket);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getTempCredential() {
        try {
            String policy = "{\"version\":\"2.0\",\"statement\":[{\"effect\":\"allow\",\"action\":[\"cos:PutObject\",\"cos:PostObject\"],\"resource\":[\"qcs::cos:" + region + ":uid/*:" + bucket + "/posts/*\"]}]}";

            Map<String, Object> reqBody = new LinkedHashMap<>();
            reqBody.put("Name", "xingqiu-upload");
            reqBody.put("DurationSeconds", (long) durationSeconds);
            reqBody.put("Policy", policy);

            String payload = objectMapper.writeValueAsString(reqBody);
            String authorization = TencentCloudSigner.sign(
                    secretId, secretKey, SERVICE, HOST, ACTION, VERSION, region, payload);

            String response = restClient.post()
                    .uri("https://" + HOST)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Host", HOST)
                    .header("X-TC-Action", ACTION)
                    .header("X-TC-Version", VERSION)
                    .header("X-TC-Region", region)
                    .header("X-TC-Timestamp", String.valueOf(System.currentTimeMillis() / 1000))
                    .header("Authorization", authorization)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            Map<String, Object> resp = objectMapper.readValue(response, Map.class);
            Map<String, Object> respData = (Map<String, Object>) resp.get("Response");
            Map<String, Object> credentials = (Map<String, Object>) respData.get("Credentials");

            String uploadPrefix = "posts/" + System.currentTimeMillis() / 1000 + "/";

            Map<String, Object> result = new HashMap<>();
            result.put("tmpSecretId", credentials.get("TmpSecretId"));
            result.put("tmpSecretKey", credentials.get("TmpSecretKey"));
            result.put("sessionToken", credentials.get("Token"));
            result.put("expiredTime", respData.get("ExpiredTime"));
            result.put("uploadPrefix", uploadPrefix);
            result.put("bucket", bucket);
            result.put("region", region);

            log.info("STS temp credential generated");
            return result;

        } catch (Exception e) {
            log.error("STS credential generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("获取上传凭证失败: " + e.getMessage(), e);
        }
    }
}
