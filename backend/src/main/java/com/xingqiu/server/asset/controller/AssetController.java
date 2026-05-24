package com.xingqiu.server.asset.controller;

import com.xingqiu.server.asset.domain.TrusteeshipSlot;
import com.xingqiu.server.asset.domain.UserAsset;
import com.xingqiu.server.asset.dto.TrusteeshipRequest;
import com.xingqiu.server.asset.service.TrusteeshipService;
import com.xingqiu.server.common.response.ApiResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {

    private final TrusteeshipService trusteeshipService;

    public AssetController(TrusteeshipService trusteeshipService) {
        this.trusteeshipService = trusteeshipService;
    }

    /**
     * 获取用户机器人/设备列表
     */
    @GetMapping("/robots")
    public ApiResponse<List<UserAsset>> getUserAssets() {
        Long userId = currentUserId();
        List<UserAsset> assets = trusteeshipService.getUserAssets(userId);
        return ApiResponse.ok(assets);
    }

    /**
     * 获取资产详情
     */
    @GetMapping("/robots/{assetId}")
    public ApiResponse<Map<String, Object>> getAssetDetail(@PathVariable Long assetId) {
        Map<String, Object> detail = trusteeshipService.getAssetDetail(currentUserId(), assetId);
        return ApiResponse.ok(detail);
    }

    /**
     * 创建托管
     */
    @PostMapping("/robots/{assetId}/trusteeship")
    public ApiResponse<TrusteeshipSlot> createTrusteeship(@PathVariable Long assetId,
                                                          @Valid @RequestBody TrusteeshipRequest request) {
        Long userId = currentUserId();
        request.setAssetId(assetId);
        TrusteeshipSlot slot = trusteeshipService.createTrusteeship(userId, request);
        return ApiResponse.ok(slot);
    }

    /**
     * 资产仪表盘
     */
    @GetMapping("/robots/{assetId}/dashboard")
    public ApiResponse<Map<String, Object>> getDashboard(@PathVariable Long assetId) {
        Map<String, Object> dashboard = trusteeshipService.getAssetDashboard(currentUserId(), assetId);
        return ApiResponse.ok(dashboard);
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
