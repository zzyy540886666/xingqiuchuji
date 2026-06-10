package com.xingqiu.server.distribution.controller;

import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.distribution.dto.BindInviteRequest;
import com.xingqiu.server.distribution.dto.CommissionResponse;
import com.xingqiu.server.distribution.dto.DistributionInfoResponse;
import com.xingqiu.server.distribution.dto.DistributionTeamResponse;
import com.xingqiu.server.distribution.service.CommissionService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/distribution")
public class DistributionController {

    private final CommissionService commissionService;

    public DistributionController(CommissionService commissionService) {
        this.commissionService = commissionService;
    }

    /**
     * Bind invite code (called during registration).
     */
    @PostMapping("/bind")
    public ApiResponse<Void> bindInvite(@Valid @RequestBody BindInviteRequest request) {
        Long userId = currentUserId();
        commissionService.bindInvite(userId, request.getInviteCode());
        return ApiResponse.ok(null);
    }

    /**
     * Get my distribution info: invite code, URL, team stats.
     */
    @GetMapping("/me")
    public ApiResponse<DistributionInfoResponse> getMyInfo() {
        Long userId = currentUserId();
        DistributionInfoResponse response = commissionService.getMyInfo(userId);
        return ApiResponse.ok(response);
    }

    /**
     * Get my team stats.
     */
    @GetMapping("/team")
    public ApiResponse<DistributionTeamResponse> getTeam() {
        Long userId = currentUserId();
        return ApiResponse.ok(commissionService.getTeam(userId));
    }

    /**
     * Get my commissions with pagination and optional status filter.
     */
    @GetMapping("/commissions")
    public ApiResponse<PageResult<CommissionResponse>> getCommissions(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = currentUserId();
        PageResult<CommissionResponse> result = commissionService.getMyCommissions(userId, status, page, pageSize);
        return ApiResponse.ok(result);
    }

    // ---- Risk control endpoints ----

    /**
     * Freeze a commission entry (admin/manual review).
     */
    @PostMapping("/commissions/{entryId}/freeze")
    public ApiResponse<Void> freezeCommission(
            @PathVariable Long entryId,
            @RequestParam(defaultValue = "人工冻结") String reason) {
        commissionService.freezeCommission(entryId, reason);
        return ApiResponse.ok(null);
    }

    /**
     * Handle order refund: freeze or clawback related commissions.
     */
    @PostMapping("/commissions/refund/{orderId}")
    public ApiResponse<Void> handleRefund(@PathVariable Long orderId) {
        commissionService.handleRefund(orderId);
        return ApiResponse.ok(null);
    }

    /**
     * Check if a commission entry is anomalous (frozen due to threshold).
     */
    @GetMapping("/commissions/{entryId}/anomaly")
    public ApiResponse<Boolean> checkAnomaly(@PathVariable Long entryId) {
        boolean anomalous = commissionService.isEntryAnomalous(entryId);
        return ApiResponse.ok(anomalous);
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
