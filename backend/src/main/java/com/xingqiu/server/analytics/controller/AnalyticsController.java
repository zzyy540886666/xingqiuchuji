package com.xingqiu.server.analytics.controller;

import com.xingqiu.server.common.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    @PostMapping("/events")
    public ApiResponse<Map<String, Integer>> collectEvents(@RequestBody Map<String, Object> payload) {
        Object events = payload.get("events");
        int accepted = events instanceof List<?> list ? list.size() : 0;
        return ApiResponse.ok(Map.of("accepted", accepted));
    }
}
