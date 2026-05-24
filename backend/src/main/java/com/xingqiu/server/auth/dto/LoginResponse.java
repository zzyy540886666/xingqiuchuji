package com.xingqiu.server.auth.dto;

public class LoginResponse {

    private String accessToken;

    private Long expiresIn;

    private UserSummary user;

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public UserSummary getUser() { return user; }
    public void setUser(UserSummary user) { this.user = user; }
}
