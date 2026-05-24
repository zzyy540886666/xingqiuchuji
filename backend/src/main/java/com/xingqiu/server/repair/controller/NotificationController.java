package com.xingqiu.server.repair.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.repair.domain.Notification;
import com.xingqiu.server.repair.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<PageResult<Notification>> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        Page<Notification> result = notificationService.getUserNotifications(userId, page, pageSize);
        return ApiResponse.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @PostMapping("/read")
    public ApiResponse<Void> markAsRead(@RequestBody Map<String, List<Long>> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        List<Long> ids = body.get("ids");
        notificationService.markAsRead(userId, ids);
        return ApiResponse.ok(null);
    }
}
