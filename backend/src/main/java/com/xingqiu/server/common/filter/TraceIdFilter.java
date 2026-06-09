package com.xingqiu.server.common.filter;

import com.xingqiu.server.common.util.TraceIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final int MAX_TRACE_ID_LENGTH = 64;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientTraceId = request.getHeader(TRACE_ID_HEADER);
        if (isValidTraceId(clientTraceId)) {
            TraceIdUtil.set(clientTraceId);
        } else {
            TraceIdUtil.generate();
        }
        response.setHeader(TRACE_ID_HEADER, TraceIdUtil.get());
        try {
            filterChain.doFilter(request, response);
        } finally {
            TraceIdUtil.clear();
        }
    }

    private boolean isValidTraceId(String traceId) {
        return traceId != null
                && !traceId.isBlank()
                && traceId.length() <= MAX_TRACE_ID_LENGTH
                && traceId.matches("[A-Za-z0-9._:-]+");
    }
}
