package com.xingqiu.server.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.asset.domain.AssetRevenueLedger;
import com.xingqiu.server.asset.domain.TrusteeshipSlot;
import com.xingqiu.server.asset.domain.UserAsset;
import com.xingqiu.server.asset.dto.TrusteeshipRequest;
import com.xingqiu.server.asset.mapper.AssetMapper;
import com.xingqiu.server.asset.mapper.AssetRevenueLedgerMapper;
import com.xingqiu.server.asset.mapper.TrusteeshipSlotMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrusteeshipService {

    private static final Logger log = LoggerFactory.getLogger(TrusteeshipService.class);

    private final AssetMapper assetMapper;
    private final TrusteeshipSlotMapper slotMapper;
    private final AssetRevenueLedgerMapper revenueLedgerMapper;

    public TrusteeshipService(AssetMapper assetMapper,
                              TrusteeshipSlotMapper slotMapper,
                              AssetRevenueLedgerMapper revenueLedgerMapper) {
        this.assetMapper = assetMapper;
        this.slotMapper = slotMapper;
        this.revenueLedgerMapper = revenueLedgerMapper;
    }

    /**
     * 获取用户资产列表
     */
    public List<UserAsset> getUserAssets(Long userId) {
        LambdaQueryWrapper<UserAsset> query = new LambdaQueryWrapper<>();
        query.eq(UserAsset::getUserId, userId)
             .orderByDesc(UserAsset::getAcquiredAt);
        return assetMapper.selectList(query);
    }

    /**
     * 获取资产详情 + 活跃托管位
     */
    public Map<String, Object> getAssetDetail(Long userId, Long assetId) {
        UserAsset asset = ownedAsset(userId, assetId);

        LambdaQueryWrapper<TrusteeshipSlot> slotQuery = new LambdaQueryWrapper<>();
        slotQuery.eq(TrusteeshipSlot::getAssetId, assetId)
                 .eq(TrusteeshipSlot::getStatus, "ACTIVE")
                 .orderByDesc(TrusteeshipSlot::getCreatedAt);
        List<TrusteeshipSlot> activeSlots = slotMapper.selectList(slotQuery);

        Map<String, Object> result = new HashMap<>();
        result.put("asset", asset);
        result.put("activeSlots", activeSlots);
        return result;
    }

    /**
     * 创建托管：校验时段冲突，插入托管位
     */
    @Transactional
    public TrusteeshipSlot createTrusteeship(Long userId, TrusteeshipRequest request) {
        // 校验资产存在且属于该用户
        UserAsset asset = assetMapper.selectById(request.getAssetId());
        if (asset == null) {
            throw new BizException(ErrorCode.ASSET_NOT_FOUND, "资产不存在: " + request.getAssetId());
        }
        if (!asset.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.ASSET_NOT_FOUND, "无权操作此资产");
        }

        // 校验时段冲突：同一资产已有 ACTIVE slot 与 newSlot 时段重叠
        LocalDateTime newStart = request.getStartTime();
        LocalDateTime newEnd = request.getEndTime();
        if (!newEnd.isAfter(newStart)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "托管结束时间必须晚于开始时间");
        }

        LambdaQueryWrapper<TrusteeshipSlot> conflictQuery = new LambdaQueryWrapper<>();
        conflictQuery.eq(TrusteeshipSlot::getAssetId, request.getAssetId())
                     .eq(TrusteeshipSlot::getStatus, "ACTIVE")
                     .lt(TrusteeshipSlot::getStartTime, newEnd)
                     .gt(TrusteeshipSlot::getEndTime, newStart);
        Long conflictCount = slotMapper.selectCount(conflictQuery);
        if (conflictCount != null && conflictCount > 0) {
            throw new BizException(ErrorCode.ASSET_SLOT_CONFLICT,
                    "托管时段冲突: assetId=" + request.getAssetId() + " start=" + newStart + " end=" + newEnd);
        }

        TrusteeshipSlot slot = new TrusteeshipSlot();
        slot.setAssetId(request.getAssetId());
        slot.setUserId(userId);
        slot.setStartTime(newStart);
        slot.setEndTime(newEnd);
        slot.setStatus("ACTIVE");
        slot.setDailyRateMinor(request.getDailyRateMinor());
        slot.setTotalRevenueMinor(0L);
        slot.setCreatedAt(LocalDateTime.now());
        slotMapper.insert(slot);

        log.info("trusteeship created slotId={} assetId={} userId={} start={} end={}",
                slot.getId(), request.getAssetId(), userId, newStart, newEnd);

        return slot;
    }

    /**
     * 资产仪表盘：累计收益 + 活跃托管位数量
     */
    public Map<String, Object> getAssetDashboard(Long userId, Long assetId) {
        UserAsset asset = ownedAsset(userId, assetId);

        // 累加已结算 + 待结算收益
        LambdaQueryWrapper<AssetRevenueLedger> revenueQuery = new LambdaQueryWrapper<>();
        revenueQuery.eq(AssetRevenueLedger::getAssetId, assetId);
        List<AssetRevenueLedger> revenues = revenueLedgerMapper.selectList(revenueQuery);
        long totalRevenueMinor = revenues.stream()
                .mapToLong(r -> r.getAmountMinor() != null ? r.getAmountMinor() : 0L)
                .sum();
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        long monthRevenueMinor = revenues.stream()
                .filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().isBefore(monthStart))
                .mapToLong(r -> r.getAmountMinor() != null ? r.getAmountMinor() : 0L)
                .sum();

        // 活跃托管位数量
        LambdaQueryWrapper<TrusteeshipSlot> activeQuery = new LambdaQueryWrapper<>();
        activeQuery.eq(TrusteeshipSlot::getAssetId, assetId)
                   .eq(TrusteeshipSlot::getStatus, "ACTIVE");
        long activeSlotCount = slotMapper.selectCount(activeQuery);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("assetId", assetId);
        dashboard.put("deviceStatus", asset.getStatus());
        dashboard.put("totalRevenueMinor", totalRevenueMinor);
        dashboard.put("monthRevenueMinor", monthRevenueMinor);
        dashboard.put("activeSlotCount", activeSlotCount);
        dashboard.put("revenueRecords", revenues);
        return dashboard;
    }

    private UserAsset ownedAsset(Long userId, Long assetId) {
        UserAsset asset = assetMapper.selectById(assetId);
        if (asset == null || !asset.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.ASSET_NOT_FOUND, "资产不存在或无访问权限");
        }
        return asset;
    }
}
