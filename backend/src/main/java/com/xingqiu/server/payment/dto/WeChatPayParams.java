package com.xingqiu.server.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WeChatPayParams {

    private String timeStamp;

    private String nonceStr;

    @JsonProperty("package")
    private String packageVal;

    private String signType;

    private String paySign;

    public String getTimeStamp() { return timeStamp; }
    public void setTimeStamp(String timeStamp) { this.timeStamp = timeStamp; }

    public String getNonceStr() { return nonceStr; }
    public void setNonceStr(String nonceStr) { this.nonceStr = nonceStr; }

    public String getPackageVal() { return packageVal; }
    public void setPackageVal(String packageVal) { this.packageVal = packageVal; }

    public String getSignType() { return signType; }
    public void setSignType(String signType) { this.signType = signType; }

    public String getPaySign() { return paySign; }
    public void setPaySign(String paySign) { this.paySign = paySign; }
}
