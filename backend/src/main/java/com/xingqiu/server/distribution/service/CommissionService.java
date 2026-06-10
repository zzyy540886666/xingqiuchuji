package com.xingqiu.server.distribution.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.auth.mapper.UserMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.distribution.domain.*;
import com.xingqiu.server.distribution.dto.CommissionResponse;
import com.xingqiu.server.distribution.dto.DistributionInfoResponse;
import com.xingqiu.server.distribution.dto.DistributionTeamResponse;
import com.xingqiu.server.distribution.mapper.CommissionEntryMapper;
import com.xingqiu.server.distribution.mapper.CommissionSettlementBatchMapper;
import com.xingqiu.server.distribution.mapper.DistributionMapper;
import com.xingqiu.server.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommissionService {

    private static final Logger log = LoggerFactory.getLogger(CommissionService.class);

    /** Commission rates in integer percent. */
    private static final int LEVEL1_RATE_PERCENT = 5;
    private static final int LEVEL2_RATE_PERCENT = 3;

    /** Protection period: 7 days. */
    private static final int PROTECT_DAYS = 7;

    /** Anomaly threshold: commission > 100 yuan (10000 fen) triggers manual review flag. */
    private static final long ANOMALY_THRESHOLD_MINOR = 100_00L;

    /** Commission entry status values. */
    public static final String STATUS_PENDING_PROTECT = "PENDING_PROTECT";
    public static final String STATUS_SETTLEABLE = "SETTLEABLE";
    public static final String STATUS_SETTLED = "SETTLED";
    public static final String STATUS_FROZEN = "FROZEN";
    public static final String STATUS_DISPUTE = "DISPUTE";

    private static final List<String> HOME_PENDING_STATUSES = List.of(STATUS_PENDING_PROTECT, STATUS_SETTLEABLE);
    private static final List<String> ALLOWED_QUERY_STATUSES = List.of(
            "ALL", STATUS_PENDING_PROTECT, STATUS_SETTLEABLE, STATUS_SETTLED, STATUS_FROZEN, STATUS_DISPUTE
    );
    private static final DateTimeFormatter SETTLE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final DistributionMapper distributionMapper;
    private final CommissionEntryMapper commissionEntryMapper;
    private final CommissionSettlementBatchMapper settlementBatchMapper;
    private final WalletService walletService;
    private final UserMapper userMapper;

    public CommissionService(DistributionMapper distributionMapper,
                             CommissionEntryMapper commissionEntryMapper,
                             CommissionSettlementBatchMapper settlementBatchMapper,
                             WalletService walletService,
                             UserMapper userMapper) {
        this.distributionMapper = distributionMapper;
        this.commissionEntryMapper = commissionEntryMapper;
        this.settlementBatchMapper = settlementBatchMapper;
        this.walletService = walletService;
        this.userMapper = userMapper;
    }

    // ---- Invite binding ----

    /**
     * Bind an invite relationship. The inviteCode encodes the inviter's userId.
     * Creates level-1 and level-2 relations.
     */
    @Transactional
    public void bindInvite(Long inviteeUserId, String inviteCode) {
        Long inviterUserId = decodeInviteCode(inviteCode);

        if (inviterUserId.equals(inviteeUserId)) {
            throw new BizException(ErrorCode.DISTRIBUTION_SELF_BIND);
        }

        if (userMapper.selectById(inviterUserId) == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "邀请码无效或邀请人不存在");
        }

        // Check invitee is not already bound
        LambdaQueryWrapper<InviteRelation> existingQuery = new LambdaQueryWrapper<>();
        existingQuery.eq(InviteRelation::getInviteeUserId, inviteeUserId);
        if (distributionMapper.selectCount(existingQuery) > 0) {
            log.info("invitee userId={} already bound, skipping", inviteeUserId);
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // Level 1: direct inviter → invitee
        InviteRelation level1 = new InviteRelation();
        level1.setInviterUserId(inviterUserId);
        level1.setInviteeUserId(inviteeUserId);
        level1.setLevel(1);
        level1.setBoundAt(now);
        level1.setSource("REGISTER");
        distributionMapper.insert(level1);

        // Level 2: inviter's inviter → invitee (if exists)
        LambdaQueryWrapper<InviteRelation> level2Query = new LambdaQueryWrapper<>();
        level2Query.eq(InviteRelation::getInviteeUserId, inviterUserId)
                .eq(InviteRelation::getLevel, 1);
        InviteRelation invitersInviter = distributionMapper.selectOne(level2Query);
        if (invitersInviter != null) {
            InviteRelation level2 = new InviteRelation();
            level2.setInviterUserId(invitersInviter.getInviterUserId());
            level2.setInviteeUserId(inviteeUserId);
            level2.setLevel(2);
            level2.setBoundAt(now);
            level2.setSource("REGISTER");
            distributionMapper.insert(level2);

            log.info("Created level-2 relation: inviter={}, invitee={}", invitersInviter.getInviterUserId(), inviteeUserId);
        }

        log.info("Created level-1 relation: inviter={}, invitee={}", inviterUserId, inviteeUserId);
    }

    // ---- Commission calculation ----

    /**
     * Calculate commissions for a paid order.
     * Creates CommissionEntry records with 7-day protection period.
     */
    @Transactional
    public void calculateCommission(Long orderId, Long paidUserId, Long amountMinor) {
        LambdaQueryWrapper<InviteRelation> query = new LambdaQueryWrapper<>();
        query.eq(InviteRelation::getInviteeUserId, paidUserId)
                .orderByAsc(InviteRelation::getLevel);
        List<InviteRelation> relations = distributionMapper.selectList(query);

        LocalDateTime now = LocalDateTime.now();

        for (InviteRelation relation : relations) {
            int ratePercent = (relation.getLevel() == 1) ? LEVEL1_RATE_PERCENT : LEVEL2_RATE_PERCENT;
            long commissionAmount = amountMinor * ratePercent / 100;
            if (commissionAmount <= 0) {
                continue;
            }
            if (commissionExists(orderId, relation.getInviterUserId(), relation.getLevel())) {
                log.info("Commission already exists: orderId={}, beneficiary={}, level={}",
                        orderId, relation.getInviterUserId(), relation.getLevel());
                continue;
            }

            CommissionEntry entry = new CommissionEntry();
            entry.setOrderId(orderId);
            entry.setBeneficiaryUserId(relation.getInviterUserId());
            entry.setSourceUserId(paidUserId);
            entry.setLevel(relation.getLevel());
            entry.setAmountMinor(commissionAmount);
            entry.setRatePercent(ratePercent);
            entry.setStatus("PENDING_PROTECT");
            entry.setProtectUntil(now.plusDays(PROTECT_DAYS));
            entry.setCreatedAt(now);
            commissionEntryMapper.insert(entry);

            // Anomaly detection: flag unusually large commissions for manual review
            if (commissionAmount > ANOMALY_THRESHOLD_MINOR) {
                log.warn("Commission anomaly flagged: entryId={} orderId={} amountMinor={} "
                        + "beneficiary={} level={} — exceeds threshold of {}",
                        entry.getId(), orderId, commissionAmount,
                        relation.getInviterUserId(), relation.getLevel(),
                        ANOMALY_THRESHOLD_MINOR);
                entry.setStatus(STATUS_FROZEN);
                commissionEntryMapper.updateById(entry);
                continue;
            }

            log.info("Commission created: beneficiary={}, source={}, level={}, amount={} cents",
                    relation.getInviterUserId(), paidUserId, relation.getLevel(), commissionAmount);
        }
    }

    private boolean commissionExists(Long orderId, Long beneficiaryUserId, Integer level) {
        LambdaQueryWrapper<CommissionEntry> query = new LambdaQueryWrapper<>();
        query.eq(CommissionEntry::getOrderId, orderId)
                .eq(CommissionEntry::getBeneficiaryUserId, beneficiaryUserId)
                .eq(CommissionEntry::getLevel, level);
        Long count = commissionEntryMapper.selectCount(query);
        return count != null && count > 0;
    }

    // ---- Settlement ----

    /**
     * Settle commissions: transition PENDING_PROTECT past protection → SETTLEABLE,
     * then settle all SETTLEABLE entries.
     */
    @Transactional
    public void settleCommissions() {
        LocalDateTime now = LocalDateTime.now();

        // Step 1: transition expired protection to SETTLEABLE
        LambdaQueryWrapper<CommissionEntry> protectQuery = new LambdaQueryWrapper<>();
        protectQuery.eq(CommissionEntry::getStatus, "PENDING_PROTECT")
                .lt(CommissionEntry::getProtectUntil, now);
        List<CommissionEntry> readyEntries = commissionEntryMapper.selectList(protectQuery);
        for (CommissionEntry entry : readyEntries) {
            entry.setStatus("SETTLEABLE");
            commissionEntryMapper.updateById(entry);
        }
        if (!readyEntries.isEmpty()) {
            log.info("Transited {} entries from PENDING_PROTECT to SETTLEABLE", readyEntries.size());
        }

        // Step 2: settle SETTLEABLE entries
        LambdaQueryWrapper<CommissionEntry> settleQuery = new LambdaQueryWrapper<>();
        settleQuery.eq(CommissionEntry::getStatus, "SETTLEABLE");
        List<CommissionEntry> settleableEntries = commissionEntryMapper.selectList(settleQuery);

        if (settleableEntries.isEmpty()) {
            log.info("No settleable commission entries found");
            return;
        }

        long totalAmount = 0L;
        for (CommissionEntry entry : settleableEntries) {
            entry.setStatus("SETTLED");
            entry.setSettledAt(now);
            commissionEntryMapper.updateById(entry);
            totalAmount += entry.getAmountMinor();

            // Credit to beneficiary's wallet
            try {
                walletService.credit(
                        entry.getBeneficiaryUserId(),
                        entry.getAmountMinor(),
                        "COMMISSION",
                        entry.getId().toString(),
                        "佣金结算 (订单" + entry.getOrderId() + " L" + entry.getLevel() + ")"
                );
            } catch (Exception e) {
                log.error("Failed to credit wallet for commission entryId={}, beneficiary={}: {}",
                        entry.getId(), entry.getBeneficiaryUserId(), e.getMessage());
            }
        }

        // Create settlement batch
        CommissionSettlementBatch batch = new CommissionSettlementBatch();
        batch.setBatchNo("SETTLE-" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        batch.setSettledAt(now);
        batch.setTotalEntries(settleableEntries.size());
        batch.setTotalAmountMinor(totalAmount);
        settlementBatchMapper.insert(batch);

        log.info("Settled {} entries, totalAmount={} cents, batchNo={}",
                settleableEntries.size(), totalAmount, batch.getBatchNo());
    }

    // ---- Risk control ----

    /**
     * Freeze a commission entry manually (e.g., admin review, anomaly audit).
     * Only active entries (PENDING_PROTECT, SETTLEABLE) can be frozen.
     * Already settled or already frozen entries cannot be frozen again.
     */
    @Transactional
    public void freezeCommission(Long entryId, String reason) {
        CommissionEntry entry = commissionEntryMapper.selectById(entryId);
        if (entry == null) {
            throw new BizException(ErrorCode.COMMISSION_NOT_FOUND);
        }

        String currentStatus = entry.getStatus();
        if (STATUS_FROZEN.equals(currentStatus)) {
            log.info("Commission entryId={} already frozen, skipping", entryId);
            return;
        }

        if (STATUS_SETTLED.equals(currentStatus)) {
            throw new BizException(ErrorCode.COMMISSION_CANNOT_FREEZE,
                    "已结算的佣金不可冻结: entryId=" + entryId);
        }

        entry.setStatus(STATUS_FROZEN);
        commissionEntryMapper.updateById(entry);
        log.info("Commission frozen: entryId={} reason={}", entryId, reason);
    }

    /**
     * Handle order refund: freeze all related commission entries that haven't been settled yet.
     * Called when an order is refunded.
     * Settled commissions may require clawback (debited from wallet).
     */
    @Transactional
    public void handleRefund(Long orderId) {
        LambdaQueryWrapper<CommissionEntry> query = new LambdaQueryWrapper<>();
        query.eq(CommissionEntry::getOrderId, orderId);
        List<CommissionEntry> entries = commissionEntryMapper.selectList(query);

        if (entries.isEmpty()) {
            log.info("No commission entries found for orderId={}, nothing to clawback", orderId);
            return;
        }

        int frozenCount = 0;
        int clawbackCount = 0;

        for (CommissionEntry entry : entries) {
            String status = entry.getStatus();

            if (STATUS_SETTLED.equals(status)) {
                // Already settled: attempt clawback by debiting wallet
                try {
                    walletService.debit(
                            entry.getBeneficiaryUserId(),
                            entry.getAmountMinor(),
                            "COMMISSION_CLAWBACK",
                            entry.getId().toString(),
                            "订单退款,佣金追回 (订单" + orderId + " L" + entry.getLevel() + ")"
                    );
                    entry.setStatus(STATUS_FROZEN);
                    entry.setSettledAt(null);
                    commissionEntryMapper.updateById(entry);
                    clawbackCount++;
                    log.info("Commission clawed back: entryId={} beneficiary={} amountMinor={}",
                            entry.getId(), entry.getBeneficiaryUserId(), entry.getAmountMinor());
                } catch (Exception e) {
                    log.error("Failed to clawback commission entryId={} beneficiary={}: {}",
                            entry.getId(), entry.getBeneficiaryUserId(), e.getMessage());
                    // Even if debit fails, freeze the entry to prevent re-settlement
                    entry.setStatus(STATUS_FROZEN);
                    commissionEntryMapper.updateById(entry);
                    frozenCount++;
                }
            } else if (STATUS_FROZEN.equals(status)) {
                log.info("Commission entryId={} already frozen, skipping", entry.getId());
            } else {
                // PENDING_PROTECT or SETTLEABLE: just freeze
                entry.setStatus(STATUS_FROZEN);
                commissionEntryMapper.updateById(entry);
                frozenCount++;
                log.info("Commission frozen due to refund: entryId={}, orderId={}",
                        entry.getId(), orderId);
            }
        }

        log.info("Refund handling for orderId={} complete: frozen={}, clawed-back={}, total={}",
                orderId, frozenCount, clawbackCount, entries.size());
    }

    /**
     * Check and return whether a commission entry has been flagged for anomaly.
     */
    public boolean isEntryAnomalous(Long entryId) {
        CommissionEntry entry = commissionEntryMapper.selectById(entryId);
        if (entry == null) {
            throw new BizException(ErrorCode.COMMISSION_NOT_FOUND);
        }
        return STATUS_FROZEN.equals(entry.getStatus())
                && entry.getAmountMinor() > ANOMALY_THRESHOLD_MINOR;
    }

    // ---- Queries ----

    /**
     * Get my distribution info: invite code, URL, team stats.
     */
    public DistributionInfoResponse getMyInfo(Long userId) {
        DistributionInfoResponse resp = new DistributionInfoResponse();

        String inviteCode = encodeInviteCode(userId);
        DistributionInfoResponse.Overview overview = new DistributionInfoResponse.Overview();
        long pendingCommission = defaultZero(distributionMapper.sumCommissionByStatuses(userId, HOME_PENDING_STATUSES));
        long settledCommission = defaultZero(distributionMapper.sumCommissionByStatuses(userId, List.of(STATUS_SETTLED)));
        long withdrawnCommission = defaultZero(distributionMapper.sumSuccessfulWithdrawnCommission(userId));
        overview.setPendingCommission(pendingCommission);
        overview.setWithdrawnCommission(withdrawnCommission);
        overview.setWithdrawableCommission(Math.max(0L, settledCommission - withdrawnCommission));
        resp.setOverview(overview);
        resp.setInviteCode(inviteCode);
        resp.setInviteUrl("https://xingqiu.cn/invite?inviteCode=" + inviteCode);
        resp.setInvitePath("/pages/loading/index?inviteCode=" + inviteCode);

        DistributionInfoResponse.TeamStats stats = new DistributionInfoResponse.TeamStats();
        stats.setLevel1Count(distributionMapper.countLevel1(userId));
        stats.setLevel2Count(distributionMapper.countLevel2(userId));
        LocalDate today = LocalDate.now();
        stats.setTodayNew(distributionMapper.countTodayNew(userId, today.atStartOfDay(), today.plusDays(1).atStartOfDay()));
        stats.setTotalInvite(distributionMapper.countTotalInvite(userId));
        resp.setTeamStats(stats);

        return resp;
    }

    /**
     * Get team stats for current user.
     */
    public DistributionInfoResponse.TeamStats getTeamStats(Long userId) {
        DistributionInfoResponse.TeamStats stats = new DistributionInfoResponse.TeamStats();
        stats.setLevel1Count(distributionMapper.countLevel1(userId));
        stats.setLevel2Count(distributionMapper.countLevel2(userId));
        LocalDate today = LocalDate.now();
        stats.setTodayNew(distributionMapper.countTodayNew(userId, today.atStartOfDay(), today.plusDays(1).atStartOfDay()));
        stats.setTotalInvite(distributionMapper.countTotalInvite(userId));
        return stats;
    }

    public DistributionTeamResponse getTeam(Long userId) {
        DistributionTeamResponse response = new DistributionTeamResponse();
        response.setLevel1Count(distributionMapper.countLevel1(userId));
        response.setLevel2Count(distributionMapper.countLevel2(userId));
        response.setMembers(distributionMapper.findTeamMembers(userId));
        return response;
    }

    /**
     * Get my commissions with pagination and optional status filter.
     */
    public PageResult<CommissionResponse> getMyCommissions(Long userId, String status, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 50);
        String normalizedStatus = normalizeStatus(status);
        long offset = (long) (safePage - 1) * safePageSize;

        List<CommissionResponse> items = distributionMapper.findCommissionPage(userId, normalizedStatus, offset, safePageSize)
                .stream()
                .filter(Objects::nonNull)
                .map(this::enrichCommissionResponse)
                .collect(Collectors.toList());
        long total = distributionMapper.countCommissionPage(userId, normalizedStatus);
        return PageResult.of(items, total, safePage, safePageSize);
    }

    // ---- Internal helpers ----

    private CommissionResponse toCommissionResponse(CommissionEntry entry) {
        CommissionResponse resp = new CommissionResponse();
        resp.setId(entry.getId());
        resp.setOrderId(entry.getOrderId());
        resp.setBeneficiaryUserId(entry.getBeneficiaryUserId());
        resp.setSourceUserId(entry.getSourceUserId());
        resp.setLevel(entry.getLevel());
        resp.setAmountMinor(entry.getAmountMinor());
        resp.setRatePercent(entry.getRatePercent());
        resp.setStatus(entry.getStatus());
        resp.setProtectUntil(entry.getProtectUntil());
        resp.setSettledAt(entry.getSettledAt());
        resp.setCreatedAt(entry.getCreatedAt());
        return enrichCommissionResponse(resp);
    }

    private CommissionResponse enrichCommissionResponse(CommissionResponse response) {
        if (response == null) {
            return null;
        }
        if (response.getProductImage() == null || response.getProductImage().isBlank()) {
            response.setProductImage("/static/icons/device-placeholder.svg");
        }
        String effectiveStatus = response.getStatus();
        if (STATUS_FROZEN.equals(effectiveStatus)) {
            effectiveStatus = STATUS_DISPUTE;
        }
        response.setStatus(effectiveStatus);
        response.setSettleTime(buildSettleTime(response));
        return response;
    }

    private String buildSettleTime(CommissionResponse response) {
        if (STATUS_SETTLED.equals(response.getStatus()) && response.getSettledAt() != null) {
            return response.getSettledAt().format(SETTLE_TIME_FORMATTER);
        }
        if (STATUS_PENDING_PROTECT.equals(response.getStatus()) && response.getProtectUntil() != null) {
            return response.getProtectUntil().format(SETTLE_TIME_FORMATTER);
        }
        if (STATUS_SETTLEABLE.equals(response.getStatus())) {
            return "可立即提现";
        }
        if (STATUS_DISPUTE.equals(response.getStatus())) {
            return "风控审核中";
        }
        if (response.getProtectUntil() != null) {
            return response.getProtectUntil().format(SETTLE_TIME_FORMATTER);
        }
        return "";
    }

    private String normalizeStatus(String status) {
        String normalized = status == null ? "ALL" : status.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_QUERY_STATUSES.contains(normalized)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Unsupported commission status: " + status);
        }
        if (STATUS_DISPUTE.equals(normalized)) {
            return STATUS_FROZEN;
        }
        return normalized;
    }

    private long defaultZero(Long value) {
        return value == null ? 0L : value;
    }

    /**
     * Encode userId to a base62 invite code.
     */
    public static String encodeInviteCode(Long userId) {
        if (userId == null || userId <= 0) return "0";
        long n = userId;
        StringBuilder sb = new StringBuilder();
        while (n > 0) {
            sb.append(BASE62_CHARS.charAt((int) (n % 62)));
            n /= 62;
        }
        return sb.reverse().toString();
    }

    /**
     * Decode a base62 invite code back to userId.
     */
    public static Long decodeInviteCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "邀请码无效");
        }
        long result = 0;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            int val = BASE62_CHARS.indexOf(c);
            if (val < 0) {
                throw new BizException(ErrorCode.BAD_REQUEST, "邀请码格式无效");
            }
            result = result * 62 + val;
        }
        return result;
    }
}
