package com.xingqiu.server.wallet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class WithdrawReq {

    @NotNull(message = "提现金额不能为空")
    @Min(value = 1, message = "提现金额必须大于0")
    @Max(value = 10_000_00, message = "单笔提现上限10000元")
    private Long amountMinor;

    public Long getAmountMinor() { return amountMinor; }
    public void setAmountMinor(Long amountMinor) { this.amountMinor = amountMinor; }
}
