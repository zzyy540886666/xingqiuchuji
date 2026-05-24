package com.xingqiu.server.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.auth.domain.User;
import com.xingqiu.server.auth.mapper.UserMapper;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.wallet.domain.WalletLedger;
import com.xingqiu.server.wallet.domain.WithdrawRequest;
import com.xingqiu.server.wallet.mapper.WalletLedgerMapper;
import com.xingqiu.server.wallet.mapper.WithdrawMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/finance")
public class AdminFinanceController {

    private final WalletLedgerMapper walletLedgerMapper;
    private final WithdrawMapper withdrawMapper;
    private final UserMapper userMapper;

    public AdminFinanceController(WalletLedgerMapper walletLedgerMapper,
                                  WithdrawMapper withdrawMapper,
                                  UserMapper userMapper) {
        this.walletLedgerMapper = walletLedgerMapper;
        this.withdrawMapper = withdrawMapper;
        this.userMapper = userMapper;
    }

    @GetMapping("/wallet-flows")
    public ApiResponse<PageResult<Map<String, Object>>> listWalletFlows(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String source) {
        LambdaQueryWrapper<WalletLedger> query = new LambdaQueryWrapper<>();
        if (source != null && !source.isBlank()) {
            query.eq(WalletLedger::getRefType, source);
        }
        query.orderByDesc(WalletLedger::getCreatedAt);
        Page<WalletLedger> pageObj = new Page<>(page, pageSize);
        Page<WalletLedger> result = walletLedgerMapper.selectPage(pageObj, query);
        List<Map<String, Object>> items = result.getRecords().stream().map(this::walletRow).toList();
        return ApiResponse.ok(PageResult.of(items, result.getTotal(), page, pageSize));
    }

    @GetMapping("/withdrawals")
    public ApiResponse<PageResult<Map<String, Object>>> listWithdrawals(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<WithdrawRequest> pageObj = new Page<>(page, pageSize);
        Page<WithdrawRequest> result = withdrawMapper.selectPage(pageObj,
                new LambdaQueryWrapper<WithdrawRequest>().orderByDesc(WithdrawRequest::getCreatedAt));
        List<Map<String, Object>> items = result.getRecords().stream().map(this::withdrawRow).toList();
        return ApiResponse.ok(PageResult.of(items, result.getTotal(), page, pageSize));
    }

    @PostMapping("/withdrawals/{id}/approve")
    @Transactional
    public ApiResponse<Void> approve(@PathVariable Long id) {
        changeWithdrawStatus(id, "APPROVED", null);
        return ApiResponse.ok(null);
    }

    @PostMapping("/withdrawals/{id}/reject")
    @Transactional
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        changeWithdrawStatus(id, "REJECTED", body.get("reason"));
        return ApiResponse.ok(null);
    }

    @PostMapping("/withdrawals/{id}/retry")
    @Transactional
    public ApiResponse<Void> retry(@PathVariable Long id) {
        changeWithdrawStatus(id, "PROCESSING", null);
        return ApiResponse.ok(null);
    }

    private void changeWithdrawStatus(Long id, String status, String failReason) {
        WithdrawRequest request = withdrawMapper.selectById(id);
        if (request == null) return;
        request.setStatus(status);
        request.setFailReason(failReason);
        request.setUpdatedAt(LocalDateTime.now());
        withdrawMapper.updateById(request);
    }

    private Map<String, Object> walletRow(WalletLedger ledger) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", ledger.getId());
        row.put("userId", ledger.getUserId());
        row.put("source", ledger.getRefType());
        row.put("amountFen", "DEBIT".equals(ledger.getType()) ? -ledger.getAmountMinor() : ledger.getAmountMinor());
        row.put("balanceFen", ledger.getBalanceAfterMinor());
        row.put("description", ledger.getDescription());
        row.put("createdAt", ledger.getCreatedAt());
        return row;
    }

    private Map<String, Object> withdrawRow(WithdrawRequest request) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", request.getId());
        row.put("userId", request.getUserId());
        row.put("userName", userName(request.getUserId()));
        row.put("amountFen", request.getAmountMinor());
        row.put("status", request.getStatus());
        row.put("createdAt", request.getCreatedAt());
        row.put("failReason", request.getFailReason());
        return row;
    }

    private String userName(Long userId) {
        User user = userMapper.selectById(userId);
        return user == null ? "用户" + userId : user.getNickname();
    }
}
