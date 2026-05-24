package com.xingqiu.server.admin.controller;

import com.xingqiu.server.admin.dto.AdminCurrentResponse;
import com.xingqiu.server.admin.dto.AdminLoginRequest;
import com.xingqiu.server.admin.dto.AdminLoginResponse;
import com.xingqiu.server.admin.service.AdminAuthService;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResponse resp = adminAuthService.login(request);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/me")
    public ApiResponse<AdminCurrentResponse> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long adminId)) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return ApiResponse.ok(adminAuthService.currentAdmin(adminId));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok(null);
    }
}
