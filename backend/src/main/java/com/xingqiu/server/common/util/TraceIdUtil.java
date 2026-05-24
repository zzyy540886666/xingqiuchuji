package com.xingqiu.server.common.util;

import org.slf4j.MDC;

import java.util.UUID;

public final class TraceIdUtil {

    private static final String TRACE_ID_KEY = "traceId";

    private TraceIdUtil() {}

    public static String generate() {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put(TRACE_ID_KEY, traceId);
        return traceId;
    }

    public static String get() {
        String traceId = MDC.get(TRACE_ID_KEY);
        return traceId != null ? traceId : generate();
    }

    public static void set(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }
}
