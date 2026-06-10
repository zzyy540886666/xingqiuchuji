package com.xingqiu.server.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.appconfig.service.ConfigService;
import com.xingqiu.server.asset.domain.AssetRevenueLedger;
import com.xingqiu.server.asset.domain.TrusteeshipSlot;
import com.xingqiu.server.asset.domain.UserAsset;
import com.xingqiu.server.asset.dto.AssetEarningsResponse;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TrusteeshipService {

    private static final Logger log = LoggerFactory.getLogger(TrusteeshipService.class);

    private final AssetMapper assetMapper;
    private final TrusteeshipSlotMapper slotMapper;
    private final AssetRevenueLedgerMapper revenueLedgerMapper;
    private final ConfigService configService;

    public TrusteeshipService(AssetMapper assetMapper,
                              TrusteeshipSlotMapper slotMapper,
                              AssetRevenueLedgerMapper revenueLedgerMapper,
                              ConfigService configService) {
        this.assetMapper = assetMapper;
        this.slotMapper = slotMapper;
        this.revenueLedgerMapper = revenueLedgerMapper;
        this.configService = configService;
    }

    public List<UserAsset> getUserAssets(Long userId) {
        LambdaQueryWrapper<UserAsset> query = new LambdaQueryWrapper<>();
        query.eq(UserAsset::getUserId, userId)
                .orderByDesc(UserAsset::getAcquiredAt);
        return assetMapper.selectList(query);
    }

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

    @Transactional
    public TrusteeshipSlot createTrusteeship(Long userId, TrusteeshipRequest request) {
        UserAsset asset = assetMapper.selectById(request.getAssetId());
        if (asset == null) {
            throw new BizException(ErrorCode.ASSET_NOT_FOUND, "Asset not found: " + request.getAssetId());
        }
        if (!asset.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.ASSET_NOT_FOUND, "No permission for this asset");
        }

        LocalDateTime newStart = request.getStartTime();
        LocalDateTime newEnd = request.getEndTime();
        if (!newEnd.isAfter(newStart)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "End time must be after start time");
        }

        LambdaQueryWrapper<TrusteeshipSlot> conflictQuery = new LambdaQueryWrapper<>();
        conflictQuery.eq(TrusteeshipSlot::getAssetId, request.getAssetId())
                .eq(TrusteeshipSlot::getStatus, "ACTIVE")
                .lt(TrusteeshipSlot::getStartTime, newEnd)
                .gt(TrusteeshipSlot::getEndTime, newStart);
        Long conflictCount = slotMapper.selectCount(conflictQuery);
        if (conflictCount != null && conflictCount > 0) {
            throw new BizException(ErrorCode.ASSET_SLOT_CONFLICT,
                    "Trusteeship time conflict: assetId=" + request.getAssetId());
        }

        Long dailyRateMinor = configService.getTrusteeshipDailyRateMinor();

        TrusteeshipSlot slot = new TrusteeshipSlot();
        slot.setAssetId(request.getAssetId());
        slot.setUserId(userId);
        slot.setStartTime(newStart);
        slot.setEndTime(newEnd);
        slot.setStatus("ACTIVE");
        slot.setDailyRateMinor(dailyRateMinor);
        slot.setTotalRevenueMinor(0L);
        slot.setCreatedAt(LocalDateTime.now());
        slotMapper.insert(slot);

        asset.setStatus("TRUSTEED");
        assetMapper.updateById(asset);

        log.info("trusteeship created slotId={} assetId={} userId={} start={} end={} dailyRateMinor={}",
                slot.getId(), request.getAssetId(), userId, newStart, newEnd, dailyRateMinor);

        return slot;
    }

    public Map<String, Object> getAssetDashboard(Long userId, Long assetId) {
        UserAsset asset = ownedAsset(userId, assetId);

        LambdaQueryWrapper<AssetRevenueLedger> revenueQuery = new LambdaQueryWrapper<>();
        revenueQuery.eq(AssetRevenueLedger::getAssetId, assetId);
        List<AssetRevenueLedger> revenues = revenueLedgerMapper.selectList(revenueQuery);
        long totalRevenueMinor = sumAmount(revenues);

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long monthRevenueMinor = revenues.stream()
                .filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().isBefore(monthStart))
                .mapToLong(r -> safeAmount(r.getAmountMinor()))
                .sum();

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

    public AssetEarningsResponse getUserEarnings(Long userId, int days) {
        int rangeDays = days == 30 ? 30 : 7;

        LambdaQueryWrapper<UserAsset> assetQuery = new LambdaQueryWrapper<>();
        assetQuery.eq(UserAsset::getUserId, userId);
        List<UserAsset> assets = assetMapper.selectList(assetQuery);
        Map<Long, UserAsset> assetById = assets.stream()
                .collect(Collectors.toMap(UserAsset::getId, Function.identity(), (left, right) -> left));

        LambdaQueryWrapper<AssetRevenueLedger> revenueQuery = new LambdaQueryWrapper<>();
        revenueQuery.eq(AssetRevenueLedger::getUserId, userId)
                .orderByDesc(AssetRevenueLedger::getCreatedAt);
        List<AssetRevenueLedger> revenues = revenueLedgerMapper.selectList(revenueQuery);

        long totalRevenueMinor = sumAmount(revenues);
        long pendingRevenueMinor = revenues.stream()
                .filter(r -> "PENDING_SETTLE".equals(r.getStatus()))
                .mapToLong(r -> safeAmount(r.getAmountMinor()))
                .sum();
        long withdrawableRevenueMinor = revenues.stream()
                .filter(r -> "SETTLED".equals(r.getStatus()))
                .mapToLong(r -> safeAmount(r.getAmountMinor()))
                .sum();

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long monthRevenueMinor = revenues.stream()
                .filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().isBefore(monthStart))
                .mapToLong(r -> safeAmount(r.getAmountMinor()))
                .sum();

        List<AssetEarningsResponse.TrendPoint> trend = buildTrend(revenues, rangeDays);
        long trendTotal = trend.stream().mapToLong(AssetEarningsResponse.TrendPoint::getAmountMinor).sum();

        List<AssetEarningsResponse.DetailItem> details = revenues.stream()
                .limit(50)
                .map(r -> toEarningsDetail(r, assetById.get(r.getAssetId())))
                .collect(Collectors.toList());

        AssetEarningsResponse response = new AssetEarningsResponse();
        response.setTotalRevenueMinor(totalRevenueMinor);
        response.setPendingRevenueMinor(pendingRevenueMinor);
        response.setWithdrawableRevenueMinor(withdrawableRevenueMinor);
        response.setMonthRevenueMinor(monthRevenueMinor);
        response.setTrendTotalMinor(trendTotal);
        response.setActiveDeviceCount(activeTrusteeshipAssetIds(userId).size());
        response.setUpdatedAt(LocalDateTime.now());
        response.setTrend(trend);
        response.setCategories(Collections.singletonList(
                new AssetEarningsResponse.CategorySummary("TRUSTEESHIP", "托管收益", totalRevenueMinor)
        ));
        response.setDetails(details);
        return response;
    }

    @Transactional
    public Map<String, Object> inspectTrusteeships() {
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<TrusteeshipSlot> expiredQuery = new LambdaQueryWrapper<>();
        expiredQuery.eq(TrusteeshipSlot::getStatus, "ACTIVE")
                .le(TrusteeshipSlot::getEndTime, now);
        List<TrusteeshipSlot> expiredSlots = slotMapper.selectList(expiredQuery);

        int completed = 0;
        long totalRevenue = 0L;

        for (TrusteeshipSlot slot : expiredSlots) {
            try {
                long days = ChronoUnit.DAYS.between(slot.getStartTime(), slot.getEndTime());
                if (days < 1) {
                    days = 1;
                }
                long revenue = days * safeAmount(slot.getDailyRateMinor());

                slot.setStatus("COMPLETED");
                slot.setTotalRevenueMinor(revenue);
                slotMapper.updateById(slot);

                UserAsset asset = assetMapper.selectById(slot.getAssetId());
                if (asset != null && "TRUSTEED".equals(asset.getStatus())) {
                    asset.setStatus("IDLE");
                    assetMapper.updateById(asset);
                }

                AssetRevenueLedger ledger = new AssetRevenueLedger();
                ledger.setAssetId(slot.getAssetId());
                ledger.setUserId(slot.getUserId());
                ledger.setAmountMinor(revenue);
                ledger.setStatus("PENDING_SETTLE");
                ledger.setCreatedAt(now);
                revenueLedgerMapper.insert(ledger);

                completed++;
                totalRevenue += revenue;
            } catch (Exception e) {
                log.error("Failed to process trusteeship slotId={}: {}", slot.getId(), e.getMessage(), e);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("completed", completed);
        result.put("totalRevenueMinor", totalRevenue);
        return result;
    }

    private List<AssetEarningsResponse.TrendPoint> buildTrend(List<AssetRevenueLedger> revenues, int rangeDays) {
        LocalDate today = LocalDate.now();
        LocalDate rangeStart = today.minusDays(rangeDays - 1L);
        Map<String, Long> trendByDate = revenues.stream()
                .filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().toLocalDate().isBefore(rangeStart))
                .collect(Collectors.groupingBy(
                        r -> r.getCreatedAt().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        Collectors.summingLong(r -> safeAmount(r.getAmountMinor()))
                ));

        List<AssetEarningsResponse.TrendPoint> trend = new ArrayList<>();
        for (int i = rangeDays - 1; i >= 0; i--) {
            String date = today.minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE);
            trend.add(new AssetEarningsResponse.TrendPoint(date, trendByDate.getOrDefault(date, 0L)));
        }
        return trend;
    }

    private UserAsset ownedAsset(Long userId, Long assetId) {
        UserAsset asset = assetMapper.selectById(assetId);
        if (asset == null || !asset.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.ASSET_NOT_FOUND, "Asset not found or forbidden");
        }
        return asset;
    }

    private Set<Long> activeTrusteeshipAssetIds(Long userId) {
        LambdaQueryWrapper<TrusteeshipSlot> activeQuery = new LambdaQueryWrapper<>();
        activeQuery.eq(TrusteeshipSlot::getUserId, userId)
                .eq(TrusteeshipSlot::getStatus, "ACTIVE");
        return slotMapper.selectList(activeQuery).stream()
                .map(TrusteeshipSlot::getAssetId)
                .collect(Collectors.toSet());
    }

    private AssetEarningsResponse.DetailItem toEarningsDetail(AssetRevenueLedger ledger, UserAsset asset) {
        AssetEarningsResponse.DetailItem item = new AssetEarningsResponse.DetailItem();
        item.setId(ledger.getId());
        item.setAssetId(ledger.getAssetId());
        item.setAssetName(asset != null ? asset.getName() : "");
        item.setAssetImageUrl(asset != null ? asset.getImageUrl() : "");
        item.setType("TRUSTEESHIP");
        item.setAmountMinor(safeAmount(ledger.getAmountMinor()));
        item.setStatus(ledger.getStatus());
        item.setCreatedAt(ledger.getCreatedAt());
        return item;
    }

    private long sumAmount(List<AssetRevenueLedger> revenues) {
        return revenues.stream().mapToLong(r -> safeAmount(r.getAmountMinor())).sum();
    }

    private long safeAmount(Long amountMinor) {
        return amountMinor != null ? amountMinor : 0L;
    }
}
