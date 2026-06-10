package com.xingqiu.server.distribution.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BindInviteRequest {

    @NotBlank(message = "邀请码不能为空")
    @Size(max = 32, message = "邀请码长度不能超过32位")
    private String inviteCode;

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
}
