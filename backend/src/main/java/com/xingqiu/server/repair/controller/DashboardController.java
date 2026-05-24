package com.xingqiu.server.repair.controller;

import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.repair.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/work-orders")
    public ApiResponse<Map<String, Object>> getWorkOrderStats(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) String area) {
        requireAdmin();
        Map<String, Object> stats = dashboardService.getWorkOrderStats(startDate, endDate, area);
        return ApiResponse.ok(stats);
    }

    @GetMapping("/inspections")
    public ApiResponse<Map<String, Object>> getInspectionStats(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) String area) {
        requireAdmin();
        Map<String, Object> stats = dashboardService.getInspectionStats(startDate, endDate, area);
        return ApiResponse.ok(stats);
    }

    @GetMapping("/efficiency")
    public ApiResponse<List<Map<String, Object>>> getEfficiencyRankings(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        requireAdmin();
        List<Map<String, Object>> rankings = dashboardService.getEfficiencyStats(startDate, endDate);
        return ApiResponse.ok(rankings);
    }

    private void requireAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
    }
}
