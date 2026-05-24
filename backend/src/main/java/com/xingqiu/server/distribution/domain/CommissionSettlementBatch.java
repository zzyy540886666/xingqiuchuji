package com.xingqiu.server.distribution.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("commission_settlement_batches")
public class CommissionSettlementBatch {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String batchNo;
    private LocalDateTime settledAt;
    private Integer totalEntries;
    private Long totalAmountMinor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }
    public Integer getTotalEntries() { return totalEntries; }
    public void setTotalEntries(Integer totalEntries) { this.totalEntries = totalEntries; }
    public Long getTotalAmountMinor() { return totalAmountMinor; }
    public void setTotalAmountMinor(Long totalAmountMinor) { this.totalAmountMinor = totalAmountMinor; }
}
