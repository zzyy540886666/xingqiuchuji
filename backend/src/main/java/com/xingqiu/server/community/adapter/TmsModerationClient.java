package com.xingqiu.server.community.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingqiu.server.common.util.TencentCloudSigner;
import com.xingqiu.server.community.domain.Post;
import com.xingqiu.server.community.domain.PostAuditLog;
import com.xingqiu.server.community.domain.PostStatus;
import com.xingqiu.server.community.mapper.PostAuditLogMapper;
import com.xingqiu.server.community.mapper.PostMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 腾讯云内容安全（TMS）适配器（真实实现 — TC3-HMAC-SHA256 签名）
 */
@Service
public class TmsModerationClient {

    private static final Logger log = LoggerFactory.getLogger(TmsModerationClient.class);
    private static final String HOST = "cms.tencentcloudapi.com";
    private static final String SERVICE = "cms";
    private static final String VERSION = "2019-03-21";
    private static final String ACTION = "TextModeration";

    private final RestClient restClient;
    private final String secretId;
    private final String secretKey;
    private final String region;
    private final PostMapper postMapper;
    private final PostAuditLogMapper postAuditLogMapper;
    private final ObjectMapper objectMapper;

    public TmsModerationClient(
            @Value("${xingqiu.tms.secret-id}") String secretId,
            @Value("${xingqiu.tms.secret-key}") String secretKey,
            @Value("${xingqiu.tms.region:ap-guangzhou}") String region,
            PostMapper postMapper,
            PostAuditLogMapper postAuditLogMapper,
            ObjectMapper objectMapper) {
        this.restClient = RestClient.create();
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.region = region;
        this.postMapper = postMapper;
        this.postAuditLogMapper = postAuditLogMapper;
        this.objectMapper = objectMapper;
        log.info("TMS Moderation Client initialized — region: {}", region);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> moderateText(String text) {
        try {
            Map<String, Object> reqBody = new LinkedHashMap<>();
            reqBody.put("Content", text);

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
            String suggestion = (String) respData.get("Suggestion");

            log.info("TMS moderation: suggestion={}, label={}", suggestion, respData.get("Label"));
            return Map.of("suggestion", suggestion.toUpperCase());

        } catch (Exception e) {
            log.error("TMS moderation failed, routing to MANUAL_REVIEW: {}", e.getMessage(), e);
            return Map.of("suggestion", "MANUAL_REVIEW");
        }
    }

    @Async
    public void moderateAndUpdate(Long postId, String content) {
        log.info("Async TMS moderation for postId={}", postId);
        Map<String, String> result = moderateText(content);
        String suggestion = result.get("suggestion");

        String newStatus = switch (suggestion) {
            case "PASS" -> PostStatus.APPROVED.name();
            case "BLOCK" -> PostStatus.REJECTED.name();
            default -> PostStatus.MANUAL_REVIEW.name();
        };

        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setStatus(newStatus);
            post.setUpdatedAt(LocalDateTime.now());
            postMapper.updateById(post);
        }

        PostAuditLog auditLog = new PostAuditLog();
        auditLog.setPostId(postId);
        auditLog.setAuditSource("TMS");
        auditLog.setResult(suggestion);
        try {
            auditLog.setRawResponse(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            auditLog.setRawResponse("{}");
        }
        auditLog.setCreatedAt(LocalDateTime.now());
        postAuditLogMapper.insert(auditLog);

        log.info("TMS moderation done — postId={}, status={}", postId, newStatus);
    }
}
