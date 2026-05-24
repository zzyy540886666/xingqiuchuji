package com.xingqiu.server.wallet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.wallet.adapter.WeChatTransferClient;
import com.xingqiu.server.wallet.domain.Wallet;
import com.xingqiu.server.wallet.domain.WalletLedger;
import com.xingqiu.server.wallet.domain.WithdrawRequest;
import com.xingqiu.server.wallet.mapper.WalletLedgerMapper;
import com.xingqiu.server.wallet.mapper.WalletMapper;
import com.xingqiu.server.wallet.mapper.WithdrawMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);
    private static final long MAX_WITHDRAW_WITHOUT_VERIFIED_NAME_MINOR = 199_999L;

    private final WalletMapper walletMapper;
    private final WalletLedgerMapper walletLedgerMapper;
    private final WithdrawMapper withdrawMapper;
    private final WeChatTransferClient weChatTransferClient;

    public WalletService(WalletMapper walletMapper,
                         WalletLedgerMapper walletLedgerMapper,
                         WithdrawMapper withdrawMapper,
                         WeChatTransferClient weChatTransferClient) {
        this.walletMapper = walletMapper;
        this.walletLedgerMapper = walletLedgerMapper;
        this.withdrawMapper = withdrawMapper;
        this.weChatTransferClient = weChatTransferClient;
    }

    /**
     * 获取用户钱包，不存在则创建
     */
    public Wallet getWallet(Long userId) {
        return findOrCreate(userId);
    }

    /**
     * 入账：增加余额 + 记录流水
     */
    @Transactional
    public void credit(Long userId, Long amountMinor, String refType, String refId, String description) {
        Wallet wallet = findOrCreate(userId);
        long balanceAfter = wallet.getBalanceMinor() + amountMinor;
        wallet.setBalanceMinor(balanceAfter);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletMapper.updateById(wallet);

        insertLedger(userId, "CREDIT", amountMinor, refType, refId, balanceAfter, description);
        log.info("wallet credit userId={} amountMinor={} balanceAfterMinor={} refType={} refId={}",
                userId, amountMinor, balanceAfter, refType, refId);
    }

    /**
     * 出账：扣减余额 + 记录流水
     * 校验余额充足
     */
    @Transactional
    public void debit(Long userId, Long amountMinor, String refType, String refId, String description) {
        Wallet wallet = findOrCreate(userId);
        if (wallet.getBalanceMinor() < amountMinor) {
            throw new BizException(ErrorCode.BALANCE_INSUFFICIENT,
                    "余额不足，当前余额: " + wallet.getBalanceMinor() + "分，需要: " + amountMinor + "分");
        }
        long balanceAfter = wallet.getBalanceMinor() - amountMinor;
        wallet.setBalanceMinor(balanceAfter);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletMapper.updateById(wallet);

        insertLedger(userId, "DEBIT", amountMinor, refType, refId, balanceAfter, description);
        log.info("wallet debit userId={} amountMinor={} balanceAfterMinor={} refType={} refId={}",
                userId, amountMinor, balanceAfter, refType, refId);
    }

    /**
     * 分页查询流水，按 createdAt 降序
     */
    public PageResult<WalletLedger> getLedger(Long userId, Long cursor, Integer limit) {
        int pageSize = (limit != null && limit > 0 && limit <= 100) ? limit : 20;
        long pageNum = (cursor != null && cursor > 0) ? cursor : 1;

        LambdaQueryWrapper<WalletLedger> query = new LambdaQueryWrapper<>();
        query.eq(WalletLedger::getUserId, userId)
             .orderByDesc(WalletLedger::getCreatedAt);

        Page<WalletLedger> page = new Page<>(pageNum, pageSize);
        Page<WalletLedger> result = walletLedgerMapper.selectPage(page, query);

        return PageResult.of(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    /**
     * 提现：校验余额 → 创建提现申请 → 冻结资金 → 调用微信转账 → 更新状态
     */
    @Transactional
    public WithdrawRequest withdraw(Long userId, Long amountMinor, String idempotencyKey) {
        if (amountMinor == null || amountMinor <= 0) {
            throw new BizException(ErrorCode.WITHDRAW_AMOUNT_INVALID, "提现金额必须大于0");
        }
        if (amountMinor > MAX_WITHDRAW_WITHOUT_VERIFIED_NAME_MINOR) {
            throw new BizException(ErrorCode.WITHDRAW_AMOUNT_INVALID, "提现金额超过实名认证转账限额，请联系客服处理");
        }

        // 幂等检查
        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            LambdaQueryWrapper<WithdrawRequest> idemQuery = new LambdaQueryWrapper<>();
            idemQuery.eq(WithdrawRequest::getIdempotencyKey, idempotencyKey);
            WithdrawRequest existing = withdrawMapper.selectOne(idemQuery);
            if (existing != null) {
                log.info("idempotent withdraw return existing id={}", existing.getId());
                return existing;
            }
        }

        // 校验余额
        Wallet wallet = findOrCreate(userId);
        if (wallet.getBalanceMinor() < amountMinor) {
            throw new BizException(ErrorCode.BALANCE_INSUFFICIENT,
                    "可用余额不足，当前: " + wallet.getBalanceMinor() + "分，提现: " + amountMinor + "分");
        }

        // 冻结金额
        wallet.setBalanceMinor(wallet.getBalanceMinor() - amountMinor);
        wallet.setFrozenMinor(wallet.getFrozenMinor() + amountMinor);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletMapper.updateById(wallet);

        insertLedger(userId, "FREEZE", amountMinor, "WITHDRAW", null,
                wallet.getBalanceMinor(), "提现冻结");

        // 创建提现申请
        WithdrawRequest wr = new WithdrawRequest();
        wr.setUserId(userId);
        wr.setAmountMinor(amountMinor);
        wr.setStatus("PENDING");
        wr.setIdempotencyKey(idempotencyKey);
        wr.setCreatedAt(LocalDateTime.now());
        wr.setUpdatedAt(LocalDateTime.now());
        withdrawMapper.insert(wr);

        // 调用微信转账
        try {
            wr.setStatus("PROCESSING");
            wr.setUpdatedAt(LocalDateTime.now());
            withdrawMapper.updateById(wr);

            Map<String, Object> result = weChatTransferClient.transferToWallet(wr);
            String batchId = (String) result.get("batch_id");

            wr.setStatus("SUCCESS");
            wr.setWxBatchId(batchId);
            wr.setUpdatedAt(LocalDateTime.now());
            withdrawMapper.updateById(wr);

            // 解冻并扣款
            wallet.setFrozenMinor(wallet.getFrozenMinor() - amountMinor);
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.updateById(wallet);

            insertLedger(userId, "DEBIT", amountMinor, "WITHDRAW", wr.getId().toString(),
                    wallet.getBalanceMinor(), "提现成功");

            log.info("withdraw success userId={} amountMinor={} batchId={}", userId, amountMinor, batchId);
        } catch (Exception e) {
            log.error("withdraw failed userId={} amountMinor={}", userId, amountMinor, e);

            wr.setStatus("FAILED");
            wr.setFailReason(e.getMessage());
            wr.setUpdatedAt(LocalDateTime.now());
            withdrawMapper.updateById(wr);

            // 解冻，退回余额
            wallet.setFrozenMinor(wallet.getFrozenMinor() - amountMinor);
            wallet.setBalanceMinor(wallet.getBalanceMinor() + amountMinor);
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.updateById(wallet);

            insertLedger(userId, "UNFREEZE", amountMinor, "WITHDRAW", wr.getId().toString(),
                    wallet.getBalanceMinor(), "提现失败，解冻退回");
        }

        return wr;
    }

    // ---- internal helpers ----

    private Wallet findOrCreate(Long userId) {
        LambdaQueryWrapper<Wallet> query = new LambdaQueryWrapper<>();
        query.eq(Wallet::getUserId, userId);
        Wallet wallet = walletMapper.selectOne(query);
        if (wallet == null) {
            wallet = Wallet.createDefault(userId);
            walletMapper.insert(wallet);
        }
        return wallet;
    }

    private void insertLedger(Long userId, String type, Long amountMinor, String refType,
                              String refId, Long balanceAfter, String description) {
        WalletLedger ledger = new WalletLedger();
        ledger.setUserId(userId);
        ledger.setType(type);
        ledger.setAmountMinor(amountMinor);
        ledger.setRefType(refType);
        ledger.setRefId(refId);
        ledger.setBalanceAfterMinor(balanceAfter);
        ledger.setDescription(description);
        ledger.setCreatedAt(LocalDateTime.now());
        walletLedgerMapper.insert(ledger);
    }
}
