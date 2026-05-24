package com.xingqiu.server.repair.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.repair.domain.WorkOrder;
import com.xingqiu.server.repair.domain.WorkOrderLog;
import com.xingqiu.server.repair.dto.AssignWorkOrderRequest;
import com.xingqiu.server.repair.dto.CreateWorkOrderRequest;
import com.xingqiu.server.repair.dto.RejectWorkOrderRequest;
import com.xingqiu.server.repair.dto.WorkOrderProgressRequest;
import com.xingqiu.server.repair.service.WorkOrderService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping
    public ApiResponse<WorkOrder> createWorkOrder(@RequestBody CreateWorkOrderRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        WorkOrder order = workOrderService.createWorkOrder(userId, request);
        return ApiResponse.ok(order);
    }

    @GetMapping
    public ApiResponse<PageResult<WorkOrder>> listWorkOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean isTech = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TECHNICIAN"));

        Page<WorkOrder> result = workOrderService.listWorkOrders(userId, isAdmin, isTech, status, page, pageSize);
        return ApiResponse.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getWorkOrder(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        WorkOrder order = workOrderService.getReadableWorkOrder(id, currentUserId(auth), isAdmin(auth), isTechnician(auth));
        List<WorkOrderLog> logs = workOrderService.getWorkOrderLogs(id);
        Map<String, Object> data = new HashMap<>();
        data.put("workOrder", order);
        data.put("logs", logs);
        return ApiResponse.ok(data);
    }

    @PostMapping("/{id}/assign")
    public ApiResponse<Void> assignWorkOrder(@PathVariable Long id,
                                              @RequestBody AssignWorkOrderRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        requireAdmin(auth);
        Long adminId = (Long) auth.getPrincipal();
        workOrderService.assignWorkOrder(id, request.getTechnicianId(), adminId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/accept")
    public ApiResponse<Void> acceptWorkOrder(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        requireTechnician(auth);
        Long techId = (Long) auth.getPrincipal();
        workOrderService.acceptWorkOrder(id, techId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<Void> rejectWorkOrder(@PathVariable Long id,
                                              @RequestBody RejectWorkOrderRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        requireTechnician(auth);
        Long techId = (Long) auth.getPrincipal();
        workOrderService.rejectWorkOrder(id, techId, request.getReason());
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/progress")
    public ApiResponse<Void> submitProgress(@PathVariable Long id,
                                             @RequestBody WorkOrderProgressRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        requireTechnician(auth);
        Long techId = (Long) auth.getPrincipal();
        workOrderService.submitProgress(id, techId, request);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<Void> completeWorkOrder(@PathVariable Long id,
                                                @RequestBody WorkOrderProgressRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        requireTechnician(auth);
        Long techId = (Long) auth.getPrincipal();
        workOrderService.completeWorkOrder(id, techId, request);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/verify")
    public ApiResponse<Void> verifyAndClose(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        requireAdmin(auth);
        Long adminId = (Long) auth.getPrincipal();
        workOrderService.verifyAndClose(id, adminId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/close")
    public ApiResponse<Void> closeWorkOrder(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        requireAdmin(auth);
        Long adminId = (Long) auth.getPrincipal();
        workOrderService.closeWorkOrder(id, adminId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/logs")
    public ApiResponse<List<WorkOrderLog>> getWorkOrderLogs(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        workOrderService.getReadableWorkOrder(id, currentUserId(auth), isAdmin(auth), isTechnician(auth));
        List<WorkOrderLog> logs = workOrderService.getWorkOrderLogs(id);
        return ApiResponse.ok(logs);
    }

    private void requireAdmin(Authentication auth) {
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
    }

    private void requireTechnician(Authentication auth) {
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TECHNICIAN"))) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
    }

    private Long currentUserId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private boolean isTechnician(Authentication auth) {
        return auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TECHNICIAN"));
    }
}
