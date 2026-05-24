package com.xingqiu.server.admin.controller;

import com.xingqiu.server.admin.mapper.AdminUserMapper;
import com.xingqiu.server.admin.domain.AdminUser;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/system")
public class AdminSystemController {

    private final AdminUserMapper adminUserMapper;

    public AdminSystemController(AdminUserMapper adminUserMapper) {
        this.adminUserMapper = adminUserMapper;
    }

    @GetMapping("/admins")
    public ApiResponse<PageResult<AdminUser>> listAdmins(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<AdminUser> pageObj = new Page<>(page, pageSize);
        Page<AdminUser> result = adminUserMapper.selectPage(pageObj, new LambdaQueryWrapper<AdminUser>().orderByDesc(AdminUser::getId));
        return ApiResponse.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @GetMapping("/roles")
    public ApiResponse<List<Map<String, Object>>> listRoles() {
        List<Map<String, Object>> roles = List.of(
            Map.of("id", 1, "name", "SUPER_ADMIN", "description", "超级管理员", "permissionCount", 99),
            Map.of("id", 2, "name", "OPERATOR", "description", "运营管理员", "permissionCount", 45),
            Map.of("id", 3, "name", "MODERATOR", "description", "内容审核员", "permissionCount", 12),
            Map.of("id", 4, "name", "FINANCE", "description", "财务结算员", "permissionCount", 20),
            Map.of("id", 5, "name", "SUPPORT", "description", "客服人员", "permissionCount", 15),
            Map.of("id", 6, "name", "MAINTENANCE_ADMIN", "description", "运维管理员", "permissionCount", 25),
            Map.of("id", 7, "name", "ANALYST", "description", "只读分析员", "permissionCount", 8)
        );
        return ApiResponse.ok(roles);
    }

    @GetMapping("/audit-logs")
    public ApiResponse<PageResult<Map<String, Object>>> listAuditLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(PageResult.of(List.of(), 0L, page, pageSize));
    }
}
