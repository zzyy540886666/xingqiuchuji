package com.xingqiu.server.repair.controller;

import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.repair.domain.Device;
import com.xingqiu.server.repair.service.DeviceService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/qrcodes")
public class QrCodeController {

    private final DeviceService deviceService;

    public QrCodeController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/resolve")
    public ApiResponse<Map<String, Object>> resolveQrCode(@RequestBody Map<String, String> body) {
        String qrCode = body.get("qrCode");
        Device device = deviceService.resolveQrCode(qrCode);
        Map<String, Object> result = new HashMap<>();
        result.put("device", device);
        result.put("name", device.getName());
        result.put("location", device.getLocation());
        result.put("area", device.getArea());
        return ApiResponse.ok(result);
    }
}
