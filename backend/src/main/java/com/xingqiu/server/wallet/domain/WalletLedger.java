package com.xingqiu.server.wallet.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("wallet_ledger")
public class WalletLedger {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    /** 类型: CREDIT / DEBIT / FREEZE / UNFREEZE */
    private String type;

    /** 金额，单位：分 */
    private Long amountMinor;

    /** 关联业务类型: ORDER_DISCOUNT / COMMISSION / ACTIVITY / WITHDRAW */
    private String refType;

    /** 关联业务 ID */
    private String refId;

    /** 操作后余额，单位：分 */
    private Long balanceAfterMinor;

    private String description;

    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getAmountMinor() { return amountMinor; }
    public void setAmountMinor(Long amountMinor) { this.amountMinor = amountMinor; }

    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }

    public String getRefId() { return refId; }
    public void setRefId(String refId) { this.refId = refId; }

    public Long getBalanceAfterMinor() { return balanceAfterMinor; }
    public void setBalanceAfterMinor(Long balanceAfterMinor) { this.balanceAfterMinor = balanceAfterMinor; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
