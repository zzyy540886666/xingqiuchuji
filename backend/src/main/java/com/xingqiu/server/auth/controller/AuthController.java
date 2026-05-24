package com.xingqiu.server.auth.controller;

import com.xingqiu.server.auth.dto.LoginRequest;
import com.xingqiu.server.auth.dto.LoginResponse;
import com.xingqiu.server.auth.service.WeChatLoginService;
import com.xingqiu.server.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final WeChatLoginService weChatLoginService;

    public AuthController(WeChatLoginService weChatLoginService) {
        this.weChatLoginService = weChatLoginService;
    }

    @PostMapping("/wechat")
    public ApiResponse<LoginResponse> wechatLogin(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = weChatLoginService.login(request);
        return ApiResponse.ok(response);
    }
}
