package com.xingqiu.server.job.controller;

import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.job.service.ImTokenService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class JobController {

    private final ImTokenService imTokenService;

    public JobController(ImTokenService imTokenService) {
        this.imTokenService = imTokenService;
    }

    /**
     * Get IM token for current user.
     */
    @GetMapping("/im/token")
    public ApiResponse<Map<String, Object>> getImToken() {
        Long userId = currentUserId();
        Map<String, Object> tokenInfo = imTokenService.getImToken(userId);
        return ApiResponse.ok(tokenInfo);
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
