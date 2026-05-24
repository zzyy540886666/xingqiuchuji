package com.xingqiu.server.auth.dto;

public class LoginRequest {

    private String code;

    private String inviteCode;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
}
