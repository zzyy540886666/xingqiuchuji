package com.xingqiu.server.order.dto;

import java.util.List;

public class PreviewResponse {

    private List<PriceBreakdownItem> priceBreakdown;

    /** Payable amount in cents (integer). Never use float/double for money. */
    private Long payableAmount;

    /** Deposit amount in cents (integer). Never use float/double for money. */
    private Long depositAmount;

    public List<PriceBreakdownItem> getPriceBreakdown() { return priceBreakdown; }
    public void setPriceBreakdown(List<PriceBreakdownItem> priceBreakdown) { this.priceBreakdown = priceBreakdown; }

    public Long getPayableAmount() { return payableAmount; }
    public void setPayableAmount(Long payableAmount) { this.payableAmount = payableAmount; }

    public Long getDepositAmount() { return depositAmount; }
    public void setDepositAmount(Long depositAmount) { this.depositAmount = depositAmount; }

    public static class PriceBreakdownItem {
        private String label;
        private Long amountMinor;

        public PriceBreakdownItem() {}

        public PriceBreakdownItem(String label, Long amountMinor) {
            this.label = label;
            this.amountMinor = amountMinor;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public Long getAmountMinor() { return amountMinor; }
        public void setAmountMinor(Long amountMinor) { this.amountMinor = amountMinor; }
    }
}
