package com.xingqiu.server.analytics.controller;

import com.xingqiu.server.analytics.dto.AnalyticsEventRequest;
import com.xingqiu.server.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    @PostMapping("/events")
    public ApiResponse<Map<String, Integer>> collectEvents(@Valid @RequestBody AnalyticsEventRequest request) {
        return ApiResponse.ok(Map.of("accepted", request.getEvents().size()));
    }
}
