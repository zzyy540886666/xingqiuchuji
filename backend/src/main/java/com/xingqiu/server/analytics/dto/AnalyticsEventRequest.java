package com.xingqiu.server.analytics.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public class AnalyticsEventRequest {

    @NotEmpty(message = "事件列表不能为空")
    @Size(max = 20, message = "单次最多上报20个事件")
    @Valid
    private List<EventPayload> events;

    public List<EventPayload> getEvents() {
        return events;
    }

    public void setEvents(List<EventPayload> events) {
        this.events = events;
    }

    public static class EventPayload {

        @NotBlank(message = "事件名称不能为空")
        @Size(max = 64, message = "事件名称最长64字符")
        private String event;

        @Size(max = 128, message = "页面路径最长128字符")
        private String page;

        @Size(max = 20, message = "事件参数最多20项")
        private Map<String, Object> params;

        private Long timestamp;

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public String getPage() {
            return page;
        }

        public void setPage(String page) {
            this.page = page;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public void setParams(Map<String, Object> params) {
            this.params = params;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
