package com.xingqiu.server.repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.repair.domain.Device;
import com.xingqiu.server.repair.dto.CreateDeviceRequest;
import com.xingqiu.server.repair.mapper.DeviceMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DeviceService {

    private final DeviceMapper deviceMapper;

    public DeviceService(DeviceMapper deviceMapper) {
        this.deviceMapper = deviceMapper;
    }

    public Page<Device> listDevices(String area, String status, int page, int pageSize) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        if (area != null && !area.isEmpty()) {
            wrapper.eq(Device::getArea, area);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Device::getStatus, status);
        }
        wrapper.orderByDesc(Device::getCreatedAt);
        return deviceMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    public Device getDevice(Long id) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BizException(ErrorCode.DEVICE_NOT_FOUND);
        }
        return device;
    }

    public Device createDevice(CreateDeviceRequest request) {
        Device device = new Device();
        device.setDeviceNo(request.getDeviceNo());
        device.setName(request.getName());
        device.setLocation(request.getLocation());
        device.setArea(request.getArea());
        device.setType(request.getType());
        device.setModel(request.getModel());
        device.setManufacturer(request.getManufacturer());
        device.setInstallDate(request.getInstallDate());
        device.setStatus("NORMAL");
        device.setCreatedAt(LocalDateTime.now());
        device.setUpdatedAt(LocalDateTime.now());
        deviceMapper.insert(device);
        return device;
    }

    public Device updateDevice(Long id, CreateDeviceRequest request) {
        Device device = getDevice(id);
        device.setDeviceNo(request.getDeviceNo());
        device.setName(request.getName());
        device.setLocation(request.getLocation());
        device.setArea(request.getArea());
        device.setType(request.getType());
        device.setModel(request.getModel());
        device.setManufacturer(request.getManufacturer());
        device.setInstallDate(request.getInstallDate());
        device.setUpdatedAt(LocalDateTime.now());
        deviceMapper.updateById(device);
        return device;
    }

    public String generateQrCode(Long id) {
        Device device = getDevice(id);
        String qrCode = "XQ-DEV-" + id;
        device.setQrCode(qrCode);
        device.setUpdatedAt(LocalDateTime.now());
        deviceMapper.updateById(device);
        return qrCode;
    }

    public Device resolveQrCode(String qrCode) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getQrCode, qrCode);
        Device device = deviceMapper.selectOne(wrapper);
        if (device == null) {
            throw new BizException(ErrorCode.DEVICE_NOT_FOUND, "无效的二维码");
        }
        return device;
    }
}
