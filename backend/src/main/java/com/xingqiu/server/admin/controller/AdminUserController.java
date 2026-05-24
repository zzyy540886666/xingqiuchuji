package com.xingqiu.server.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.admin.service.AdminUserService;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.member.domain.Membership;
import com.xingqiu.server.member.mapper.MembershipMapper;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final MembershipMapper membershipMapper;

    public AdminUserController(AdminUserService adminUserService, MembershipMapper membershipMapper) {
        this.adminUserService = adminUserService;
        this.membershipMapper = membershipMapper;
    }

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<com.xingqiu.server.auth.domain.User> result = adminUserService.listUsers(keyword, role, page, pageSize);
        List<Map<String, Object>> items = result.getItems().stream()
                .map(user -> toRow(user))
                .filter(row -> status == null || status.isBlank() || status.equals(row.get("status")))
                .toList();
        return ApiResponse.ok(PageResult.of(items, items.size(), page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getUserDetail(@PathVariable Long id) {
        return ApiResponse.ok(toRow(adminUserService.getUserDetail(id)));
    }

    @PatchMapping("/{id}/role")
    public ApiResponse<Void> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminUserService.updateUserRole(id, body.get("role"));
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        adminUserService.toggleUserStatus(id, Boolean.TRUE.equals(body.get("enabled")));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/toggle-status")
    public ApiResponse<Void> toggleStatusByReason(@PathVariable Long id) {
        com.xingqiu.server.auth.domain.User user = adminUserService.getUserDetail(id);
        adminUserService.toggleUserStatus(id, "DISABLED".equals(user.getRole()));
        return ApiResponse.ok(null);
    }

    private Map<String, Object> toRow(com.xingqiu.server.auth.domain.User user) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", user.getId());
        row.put("openid", user.getOpenid());
        row.put("nickname", user.getNickname());
        row.put("avatarUrl", user.getAvatarUrl());
        Membership membership = membershipMapper.selectOne(new LambdaQueryWrapper<Membership>()
                .eq(Membership::getUserId, user.getId()));
        row.put("phone", null);
        row.put("memberLevel", "Lv." + (membership == null ? 0 : membership.getLevel()));
        row.put("role", user.getRole());
        row.put("status", "DISABLED".equals(user.getRole()) ? "FROZEN" : "ACTIVE");
        row.put("createdAt", user.getCreatedAt());
        row.put("updatedAt", user.getUpdatedAt());
        return row;
    }

}
