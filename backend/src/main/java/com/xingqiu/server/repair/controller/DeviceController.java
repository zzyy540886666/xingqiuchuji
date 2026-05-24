package com.xingqiu.server.repair.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.repair.domain.Device;
import com.xingqiu.server.repair.dto.CreateDeviceRequest;
import com.xingqiu.server.repair.service.DeviceService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ApiResponse<PageResult<Device>> listDevices(
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<Device> result = deviceService.listDevices(area, status, page, pageSize);
        return ApiResponse.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Device> getDevice(@PathVariable Long id) {
        Device device = deviceService.getDevice(id);
        return ApiResponse.ok(device);
    }

    @PostMapping
    public ApiResponse<Device> createDevice(@RequestBody CreateDeviceRequest request) {
        requireAdmin();
        Device device = deviceService.createDevice(request);
        return ApiResponse.ok(device);
    }

    @PatchMapping("/{id}")
    public ApiResponse<Device> updateDevice(@PathVariable Long id,
                                             @RequestBody CreateDeviceRequest request) {
        requireAdmin();
        Device device = deviceService.updateDevice(id, request);
        return ApiResponse.ok(device);
    }

    @PostMapping("/{id}/qrcode")
    public ApiResponse<String> generateQrCode(@PathVariable Long id) {
        requireAdmin();
        String qrCode = deviceService.generateQrCode(id);
        return ApiResponse.ok(qrCode);
    }

    private void requireAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
    }
}
