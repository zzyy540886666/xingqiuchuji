package com.xingqiu.server.common.filter;

import com.xingqiu.server.common.util.TraceIdUtil;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter();

    @Test
    void usesValidClientTraceId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-Trace-Id", "mp-lxyz123-abc999-1");
        AtomicReference<String> activeTraceId = new AtomicReference<>();
        FilterChain chain = (req, res) -> activeTraceId.set(TraceIdUtil.get());

        filter.doFilter(request, response, chain);

        assertThat(activeTraceId.get()).isEqualTo("mp-lxyz123-abc999-1");
        assertThat(response.getHeader("X-Trace-Id")).isEqualTo("mp-lxyz123-abc999-1");
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    void generatesTraceIdWhenClientTraceIdIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-Trace-Id", "bad trace id <script>");
        AtomicReference<String> activeTraceId = new AtomicReference<>();
        FilterChain chain = (req, res) -> activeTraceId.set(TraceIdUtil.get());

        filter.doFilter(request, response, chain);

        assertThat(activeTraceId.get()).isNotBlank();
        assertThat(activeTraceId.get()).isNotEqualTo("bad trace id <script>");
        assertThat(response.getHeader("X-Trace-Id")).isEqualTo(activeTraceId.get());
        assertThat(MDC.get("traceId")).isNull();
    }
}
