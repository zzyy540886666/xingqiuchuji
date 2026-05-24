package com.xingqiu.server.common.filter;

import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.util.TraceIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * Sliding-window rate limiter using Redis.
 * Bucket config per path group — stricter for auth/write endpoints.
 */
@Component
@Order(2)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final String PREFIX = "rl:";

    private final StringRedisTemplate redis;

    public RateLimitFilter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = getClientIp(request);
        RateLimitBucket bucket = resolveBucket(path, method);

        if (bucket == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = PREFIX + bucket.prefix + ":" + clientIp;
        Long count = redis.opsForValue().increment(key);

        if (count == 1) {
            redis.expire(key, bucket.window);
        }

        if (count != null && count > bucket.maxRequests) {
            log.warn("Rate limit exceeded: path={}, ip={}, count={}", bucket.prefix, clientIp, count);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"error\":{\"code\":\"" + ErrorCode.RATE_LIMITED.getCode()
                    + "\",\"message\":\"请求过于频繁，请稍后再试\"},\"traceId\":\"" + TraceIdUtil.get() + "\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private RateLimitBucket resolveBucket(String path, String method) {
        if (path.startsWith("/api/v1/auth/")) {
            return new RateLimitBucket("auth", 10, Duration.ofMinutes(1));
        }
        if (path.startsWith("/api/v1/orders") && !path.contains("pay")) {
            return new RateLimitBucket("order-create", 30, Duration.ofMinutes(1));
        }
        if (path.equals("/api/v1/wallet/withdraw")) {
            return new RateLimitBucket("withdraw", 3, Duration.ofMinutes(5));
        }
        if (path.startsWith("/api/v1/community/posts") && "POST".equalsIgnoreCase(method)) {
            return new RateLimitBucket("post", 5, Duration.ofMinutes(1));
        }
        if (path.startsWith("/api/v1/")) {
            return new RateLimitBucket("global", 200, Duration.ofMinutes(1));
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) {
            return xri.trim();
        }
        return request.getRemoteAddr();
    }

    private record RateLimitBucket(String prefix, int maxRequests, Duration window) {}
}