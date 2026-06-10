package com.xingqiu.server.repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ResolveQrCodeRequest {

    @NotBlank(message = "二维码不能为空")
    @Size(max = 128, message = "二维码最长128字符")
    @Pattern(regexp = "^[A-Za-z0-9:_\\-./]+$", message = "二维码格式不合法")
    private String qrCode;

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}
