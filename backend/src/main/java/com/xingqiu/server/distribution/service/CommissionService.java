package com.xingqiu.server.distribution.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final DistributionMapper distributionMapper;
    private final CommissionEntryMapper commissionEntryMapper;
    private final CommissionSettlementBatchMapper settlementBatchMapper;
    private final WalletService walletService;

    public CommissionService(DistributionMapper distributionMapper,
                             CommissionEntryMapper commissionEntryMapper,
                             CommissionSettlementBatchMapper settlementBatchMapper,
                             WalletService walletService) {
        this.distributionMapper = distributionMapper;
        this.commissionEntryMapper = commissionEntryMapper;
        this.settlementBatchMapper = settlementBatchMapper;
        this.walletService = walletService;
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

            log.info("Commission created: beneficiary={}, source={}, level={}, amount={} cents",
                    relation.getInviterUserId(), paidUserId, relation.getLevel(), commissionAmount);
        }
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

    // ---- Queries ----

    /**
     * Get my distribution info: invite code, URL, team stats.
     */
    public DistributionInfoResponse getMyInfo(Long userId) {
        DistributionInfoResponse resp = new DistributionInfoResponse();

        String inviteCode = encodeInviteCode(userId);
        resp.setInviteCode(inviteCode);
        resp.setInviteUrl("https://xingqiu.com/invite?code=" + inviteCode);

        DistributionInfoResponse.TeamStats stats = new DistributionInfoResponse.TeamStats();
        stats.setLevel1Count(distributionMapper.countLevel1(userId));
        stats.setLevel2Count(distributionMapper.countLevel2(userId));
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
        LambdaQueryWrapper<CommissionEntry> query = new LambdaQueryWrapper<>();
        query.eq(CommissionEntry::getBeneficiaryUserId, userId);
        if (status != null && !status.isEmpty()) {
            query.eq(CommissionEntry::getStatus, status);
        }
        query.orderByDesc(CommissionEntry::getCreatedAt);

        Page<CommissionEntry> mpPage = new Page<>(page, pageSize);
        Page<CommissionEntry> result = commissionEntryMapper.selectPage(mpPage, query);

        List<CommissionResponse> items = result.getRecords().stream()
                .map(this::toCommissionResponse)
                .collect(Collectors.toList());

        return PageResult.of(items, result.getTotal(), page, pageSize);
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
        return resp;
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
