package com.xingqiu.server.repair.controller;

import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.repair.dto.ChartDataResponse;
import com.xingqiu.server.repair.dto.DashboardOverview;
import com.xingqiu.server.repair.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardChartController {

    private final DashboardService dashboardService;

    public DashboardChartController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public ApiResponse<DashboardOverview> getOverview() {
        DashboardOverview overview = dashboardService.getOverview();
        return ApiResponse.ok(overview);
    }

    @GetMapping("/charts")
    public ApiResponse<ChartDataResponse> getChartData(
            @RequestParam String type,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        ChartDataResponse chartData = dashboardService.getChartData(type, startDate, endDate);
        return ApiResponse.ok(chartData);
    }
}
