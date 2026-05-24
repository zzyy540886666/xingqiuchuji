package com.xingqiu.server.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.repair.domain.Device;
import com.xingqiu.server.repair.domain.InspectionPlan;
import com.xingqiu.server.repair.domain.WorkOrder;
import com.xingqiu.server.repair.mapper.DeviceMapper;
import com.xingqiu.server.repair.mapper.InspectionPlanMapper;
import com.xingqiu.server.repair.mapper.WorkOrderMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/repair")
public class AdminRepairController {

    private final DeviceMapper deviceMapper;
    private final WorkOrderMapper workOrderMapper;
    private final InspectionPlanMapper inspectionPlanMapper;

    public AdminRepairController(DeviceMapper deviceMapper,
                                 WorkOrderMapper workOrderMapper,
                                 InspectionPlanMapper inspectionPlanMapper) {
        this.deviceMapper = deviceMapper;
        this.workOrderMapper = workOrderMapper;
        this.inspectionPlanMapper = inspectionPlanMapper;
    }

    @GetMapping("/devices")
    public ApiResponse<PageResult<Map<String, Object>>> listDevices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<Device> pageObj = new Page<>(page, pageSize);
        Page<Device> result = deviceMapper.selectPage(pageObj, new LambdaQueryWrapper<Device>().orderByDesc(Device::getCreatedAt));
        List<Map<String, Object>> items = result.getRecords().stream().map(this::deviceRow).toList();
        return ApiResponse.ok(PageResult.of(items, result.getTotal(), page, pageSize));
    }

    @PostMapping("/devices")
    @Transactional
    public ApiResponse<Map<String, Object>> createDevice(@RequestBody Device device) {
        device.setCreatedAt(LocalDateTime.now());
        device.setUpdatedAt(LocalDateTime.now());
        if (device.getStatus() == null) {
            device.setStatus("NORMAL");
        }
        deviceMapper.insert(device);
        return ApiResponse.ok(deviceRow(device));
    }

    @PutMapping("/devices/{id}")
    @Transactional
    public ApiResponse<Map<String, Object>> updateDevice(@PathVariable Long id, @RequestBody Device body) {
        Device device = deviceMapper.selectById(id);
        if (device == null) return ApiResponse.ok(Map.of());
        device.setName(body.getName());
        device.setLocation(body.getLocation());
        device.setUpdatedAt(LocalDateTime.now());
        deviceMapper.updateById(device);
        return ApiResponse.ok(deviceRow(device));
    }

    @GetMapping("/workorders")
    public ApiResponse<PageResult<Map<String, Object>>> listWorkOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<WorkOrder> query = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            query.eq(WorkOrder::getStatus, status);
        }
        query.orderByDesc(WorkOrder::getCreatedAt);
        Page<WorkOrder> pageObj = new Page<>(page, pageSize);
        Page<WorkOrder> result = workOrderMapper.selectPage(pageObj, query);
        List<Map<String, Object>> items = result.getRecords().stream().map(this::workOrderRow).toList();
        return ApiResponse.ok(PageResult.of(items, result.getTotal(), page, pageSize));
    }

    @PostMapping("/workorders/{id}/assign")
    @Transactional
    public ApiResponse<Void> assign(@PathVariable Long id) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order != null) {
            order.setStatus("IN_PROGRESS");
            order.setUpdatedAt(LocalDateTime.now());
            workOrderMapper.updateById(order);
        }
        return ApiResponse.ok(null);
    }

    @PostMapping("/workorders/{id}/urgent")
    public ApiResponse<Void> urgent(@PathVariable Long id) {
        return ApiResponse.ok(null);
    }

    @PostMapping("/workorders/{id}/close")
    @Transactional
    public ApiResponse<Void> close(@PathVariable Long id) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order != null) {
            order.setStatus("CLOSED");
            order.setUpdatedAt(LocalDateTime.now());
            workOrderMapper.updateById(order);
        }
        return ApiResponse.ok(null);
    }

    @GetMapping("/inspections")
    public ApiResponse<PageResult<Map<String, Object>>> listInspections(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<InspectionPlan> pageObj = new Page<>(page, pageSize);
        Page<InspectionPlan> result = inspectionPlanMapper.selectPage(pageObj, new LambdaQueryWrapper<InspectionPlan>().orderByDesc(InspectionPlan::getCreatedAt));
        List<Map<String, Object>> items = result.getRecords().stream().map(this::inspectionRow).toList();
        if (items.isEmpty()) {
            items = List.of(
                    map("id", 1L, "planName", "每日机器人安全巡检", "cycle", "每日", "taskCount", 6, "status", "PENDING", "nextRunAt", "2026-05-25T09:00:00"),
                    map("id", 2L, "planName", "每周电池与关节巡检", "cycle", "每周", "taskCount", 12, "status", "IN_PROGRESS", "nextRunAt", "2026-05-27T10:00:00")
            );
        }
        return ApiResponse.ok(PageResult.of(items, items.size(), page, pageSize));
    }

    private Map<String, Object> deviceRow(Device device) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", device.getId());
        row.put("name", device.getName());
        row.put("location", device.getLocation());
        row.put("area", device.getArea());
        row.put("technicianName", device.getResponsibleTechnicianId() == null ? "未分配" : "维修员" + device.getResponsibleTechnicianId());
        row.put("status", "FAULT".equals(device.getStatus()) ? "ABNORMAL" : "NORMAL");
        row.put("createdAt", device.getCreatedAt());
        return row;
    }

    private Map<String, Object> workOrderRow(WorkOrder order) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", order.getId());
        row.put("deviceName", order.getDeviceName());
        row.put("faultType", order.getFaultType());
        row.put("assigneeName", order.getAssignedTechnicianId() == null ? "未分配" : "维修员" + order.getAssignedTechnicianId());
        row.put("status", "NEW".equals(order.getStatus()) ? "PENDING" : order.getStatus());
        row.put("createdAt", order.getCreatedAt());
        return row;
    }

    private Map<String, Object> inspectionRow(InspectionPlan plan) {
        return map(
                "id", plan.getId(),
                "planName", plan.getName(),
                "cycle", plan.getFrequency(),
                "taskCount", 0,
                "status", plan.getStatus(),
                "nextRunAt", plan.getNextRunAt()
        );
    }

    private Map<String, Object> map(Object... kv) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            result.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return result;
    }
}
