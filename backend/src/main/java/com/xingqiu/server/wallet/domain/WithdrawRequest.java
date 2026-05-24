package com.xingqiu.server.wallet.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("withdraw_requests")
public class WithdrawRequest {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    /** 提现金额，单位：分 */
    private Long amountMinor;

    /** 状态: PENDING / PROCESSING / SUCCESS / FAILED */
    private String status;

    /** 微信转账批次号 */
    private String wxBatchId;

    /** 失败原因 */
    private String failReason;

    /** 幂等键 */
    private String idempotencyKey;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getAmountMinor() { return amountMinor; }
    public void setAmountMinor(Long amountMinor) { this.amountMinor = amountMinor; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getWxBatchId() { return wxBatchId; }
    public void setWxBatchId(String wxBatchId) { this.wxBatchId = wxBatchId; }

    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
