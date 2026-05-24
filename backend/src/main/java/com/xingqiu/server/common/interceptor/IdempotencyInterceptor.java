package com.xingqiu.server.common.interceptor;

import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyInterceptor.class);
    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";
    private static final String REDIS_PREFIX = "idem:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redis;

    public IdempotencyInterceptor(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        String key = request.getHeader(IDEMPOTENCY_HEADER);
        if (!StringUtils.hasText(key)) {
            return true; // 非幂等请求，放行
        }

        String redisKey = REDIS_PREFIX + key;
        Boolean success = redis.opsForValue().setIfAbsent(redisKey, "1", TTL);
        if (Boolean.FALSE.equals(success)) {
            log.warn("Duplicate idempotency key: {}", key);
            throw new BizException(ErrorCode.IDEMPOTENCY_CONFLICT, "重复请求");
        }
        return true;
    }
}
