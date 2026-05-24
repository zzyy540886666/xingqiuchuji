package com.xingqiu.server.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.auth.domain.User;
import com.xingqiu.server.auth.mapper.UserMapper;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.distribution.domain.CommissionEntry;
import com.xingqiu.server.distribution.domain.CommissionSettlementBatch;
import com.xingqiu.server.distribution.domain.InviteRelation;
import com.xingqiu.server.distribution.mapper.CommissionEntryMapper;
import com.xingqiu.server.distribution.mapper.CommissionSettlementBatchMapper;
import com.xingqiu.server.distribution.mapper.DistributionMapper;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/distribution")
public class AdminDistributionController {

    private final DistributionMapper distributionMapper;
    private final CommissionEntryMapper commissionEntryMapper;
    private final CommissionSettlementBatchMapper settlementBatchMapper;
    private final UserMapper userMapper;

    public AdminDistributionController(DistributionMapper distributionMapper,
                                       CommissionEntryMapper commissionEntryMapper,
                                       CommissionSettlementBatchMapper settlementBatchMapper,
                                       UserMapper userMapper) {
        this.distributionMapper = distributionMapper;
        this.commissionEntryMapper = commissionEntryMapper;
        this.settlementBatchMapper = settlementBatchMapper;
        this.userMapper = userMapper;
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        Long totalInvites = distributionMapper.selectCount(
                new LambdaQueryWrapper<InviteRelation>().eq(InviteRelation::getLevel, 1));
        Long commissionRecords = commissionEntryMapper.selectCount(null);
        Long settlementBatches = settlementBatchMapper.selectCount(null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalInvites", totalInvites);
        result.put("commissionRecords", commissionRecords);
        result.put("settlementBatches", settlementBatches);
        return ApiResponse.ok(result);
    }

    @GetMapping("/batches")
    public ApiResponse<PageResult<CommissionSettlementBatch>> listBatches(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        LambdaQueryWrapper<CommissionSettlementBatch> query = new LambdaQueryWrapper<>();
        query.orderByDesc(CommissionSettlementBatch::getSettledAt);

        Page<CommissionSettlementBatch> pageObj = new Page<>(page, pageSize);
        Page<CommissionSettlementBatch> result = settlementBatchMapper.selectPage(pageObj, query);
        return ApiResponse.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @GetMapping("/commissions")
    public ApiResponse<PageResult<Map<String, Object>>> listCommissions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<CommissionEntry> query = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            query.eq(CommissionEntry::getStatus, status);
        }
        query.orderByDesc(CommissionEntry::getCreatedAt);

        Page<CommissionEntry> pageObj = new Page<>(page, pageSize);
        Page<CommissionEntry> result = commissionEntryMapper.selectPage(pageObj, query);
        List<Map<String, Object>> items = result.getRecords().stream().map(this::commissionRow).toList();
        return ApiResponse.ok(PageResult.of(items, result.getTotal(), page, pageSize));
    }

    private Map<String, Object> commissionRow(CommissionEntry entry) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", entry.getId());
        row.put("orderId", entry.getOrderId());
        row.put("beneficiaryUserId", entry.getBeneficiaryUserId());
        row.put("beneficiaryName", userName(entry.getBeneficiaryUserId()));
        row.put("sourceUserId", entry.getSourceUserId());
        row.put("level", entry.getLevel());
        row.put("amountFen", entry.getAmountMinor());
        row.put("status", entry.getStatus());
        row.put("settledAt", entry.getSettledAt());
        return row;
    }

    private String userName(Long userId) {
        User user = userMapper.selectById(userId);
        return user == null ? "用户" + userId : user.getNickname();
    }
}
