package com.xingqiu.server.auth.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

/**
 * 微信小程序登录适配器（真实实现）
 * GET https://api.weixin.qq.com/sns/jscode2session
 */
@Service
public class WeChatMiniProgramClient {

    private static final Logger log = LoggerFactory.getLogger(WeChatMiniProgramClient.class);
    private static final String URL = "https://api.weixin.qq.com/sns/jscode2session"
            + "?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";

    private final RestClient restClient;
    private final String appId;
    private final String appSecret;
    private final ObjectMapper objectMapper;

    public WeChatMiniProgramClient(
            @Value("${xingqiu.wechat.mini-program.app-id}") String appId,
            @Value("${xingqiu.wechat.mini-program.app-secret}") String appSecret,
            ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
        this.appId = appId;
        this.appSecret = appSecret;
        this.objectMapper = objectMapper;
        log.info("WeChat MiniProgram Client initialized");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> code2session(String code) {
        if (code == null || code.isBlank()) {
            return Map.of("errcode", 40029, "errmsg", "invalid code");
        }
        if (appId == null || appId.isBlank() || appSecret == null || appSecret.isBlank()) {
            log.error("WeChat appId/appSecret is not configured");
            return Map.of("errcode", -2, "errmsg", "wechat login is not configured");
        }
        try {
            String response = restClient.get()
                    .uri(URL, appId, appSecret, code)
                    .retrieve()
                    .body(String.class);
            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            if (result.containsKey("errcode") && (int) result.get("errcode") != 0) {
                log.error("WeChat code2session error: errcode={} errmsg={}", result.get("errcode"), result.get("errmsg"));
            } else {
                log.info("WeChat code2session success");
            }
            return result;
        } catch (Exception e) {
            log.error("WeChat code2session failed: {}", e.getMessage(), e);
            return Map.of("errcode", -1, "errmsg", "network error: " + e.getMessage());
        }
    }
}
