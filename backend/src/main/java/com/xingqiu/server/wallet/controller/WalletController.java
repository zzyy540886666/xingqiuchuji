package com.xingqiu.server.wallet.controller;

import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.wallet.domain.Wallet;
import com.xingqiu.server.wallet.domain.WalletLedger;
import com.xingqiu.server.wallet.domain.WithdrawRequest;
import com.xingqiu.server.wallet.dto.WithdrawReq;
import com.xingqiu.server.wallet.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public ApiResponse<Wallet> getBalance() {
        Long userId = currentUserId();
        Wallet wallet = walletService.getWallet(userId);
        return ApiResponse.ok(wallet);
    }

    @GetMapping("/ledger")
    public ApiResponse<PageResult<WalletLedger>> getLedger(
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        Long userId = currentUserId();
        PageResult<WalletLedger> page = walletService.getLedger(userId, cursor, limit);
        return ApiResponse.ok(page);
    }

    @PostMapping("/withdraw")
    public ApiResponse<WithdrawRequest> withdraw(@Valid @RequestBody WithdrawReq req,
                                                  HttpServletRequest request) {
        Long userId = currentUserId();
        String idempotencyKey = request.getHeader(IDEMPOTENCY_HEADER);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "提现操作必须提供 Idempotency-Key 请求头");
        }
        WithdrawRequest result = walletService.withdraw(userId, req.getAmountMinor(), idempotencyKey);
        return ApiResponse.ok(result);
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
