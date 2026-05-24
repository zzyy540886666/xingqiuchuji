package com.xingqiu.server.auth.controller;

import com.xingqiu.server.auth.dto.UserSummary;
import com.xingqiu.server.auth.service.CurrentUserService;
import com.xingqiu.server.common.response.ApiResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final CurrentUserService currentUserService;

    public UserController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public ApiResponse<UserSummary> getCurrentUser() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ApiResponse.ok(currentUserService.getSummary(userId));
    }
}
