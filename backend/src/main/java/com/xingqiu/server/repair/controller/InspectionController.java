package com.xingqiu.server.repair.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.repair.domain.InspectionPlan;
import com.xingqiu.server.repair.domain.InspectionTask;
import com.xingqiu.server.repair.domain.InspectionTemplate;
import com.xingqiu.server.repair.dto.CreateInspectionPlanRequest;
import com.xingqiu.server.repair.dto.InspectionSubmitRequest;
import com.xingqiu.server.repair.service.InspectionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inspection")
public class InspectionController {

    private final InspectionService inspectionService;

    public InspectionController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @GetMapping("/templates")
    public ApiResponse<PageResult<InspectionTemplate>> listTemplates(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<InspectionTemplate> result = inspectionService.listTemplates(page, pageSize);
        return ApiResponse.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @PostMapping("/templates")
    public ApiResponse<InspectionTemplate> createTemplate(@RequestBody InspectionTemplate template) {
        requireAdmin();
        InspectionTemplate result = inspectionService.createTemplate(template);
        return ApiResponse.ok(result);
    }

    @PostMapping("/templates/{id}")
    public ApiResponse<InspectionTemplate> updateTemplate(@PathVariable Long id,
                                                           @RequestBody InspectionTemplate template) {
        requireAdmin();
        template.setId(id);
        template.setUpdatedAt(LocalDateTime.now());
        InspectionTemplate result = inspectionService.createTemplate(template);
        return ApiResponse.ok(result);
    }

    @GetMapping("/plans")
    public ApiResponse<PageResult<InspectionPlan>> listPlans(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<InspectionPlan> result = inspectionService.listPlans(status, page, pageSize);
        return ApiResponse.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @PostMapping("/plans")
    public ApiResponse<InspectionPlan> createPlan(@RequestBody CreateInspectionPlanRequest request) {
        requireAdmin();
        InspectionPlan plan = inspectionService.createPlan(request);
        return ApiResponse.ok(plan);
    }

    @PatchMapping("/plans/{id}")
    public ApiResponse<InspectionPlan> updatePlan(@PathVariable Long id,
                                                   @RequestBody CreateInspectionPlanRequest request) {
        requireAdmin();
        InspectionPlan plan = inspectionService.updatePlan(id, request);
        return ApiResponse.ok(plan);
    }

    @GetMapping("/tasks")
    public ApiResponse<PageResult<InspectionTask>> listTasks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean isTech = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TECHNICIAN"));

        Page<InspectionTask> result = inspectionService.listTasks(userId, isAdmin, isTech, status, page, pageSize);
        return ApiResponse.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @PostMapping("/tasks/{id}/start")
    public ApiResponse<InspectionTask> startTask(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        requireTechnician(auth);
        Long techId = (Long) auth.getPrincipal();
        InspectionTask task = inspectionService.startTask(id, techId);
        return ApiResponse.ok(task);
    }

    @PostMapping("/tasks/{id}/submit")
    public ApiResponse<Void> submitTask(@PathVariable Long id,
                                         @RequestBody InspectionSubmitRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        requireTechnician(auth);
        Long techId = (Long) auth.getPrincipal();
        inspectionService.submitTask(id, techId, request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/reports/summary")
    public ApiResponse<Map<String, Object>> getTaskReportSummary(
            @RequestParam(required = false) String area,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        requireAdmin();
        Map<String, Object> summary = inspectionService.getTaskReportSummary(area, startDate, endDate);
        return ApiResponse.ok(summary);
    }

    private void requireAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
    }

    private void requireTechnician(Authentication auth) {
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TECHNICIAN"))) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
    }
}
