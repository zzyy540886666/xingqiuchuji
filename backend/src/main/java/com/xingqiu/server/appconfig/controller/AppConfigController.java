package com.xingqiu.server.appconfig.controller;

import com.xingqiu.server.appconfig.service.ConfigService;
import com.xingqiu.server.common.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
public class AppConfigController {

    private final ConfigService configService;

    public AppConfigController(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * 获取应用配置。
     * 客户端传入本地版本号，服务端判断是否需要下发更新。
     */
    @GetMapping("/app")
    public ApiResponse<Map<String, Object>> getAppConfig(
            @RequestParam(required = false) Integer clientVersion) {
        Map<String, Object> config = configService.getAppConfig(clientVersion);
        return ApiResponse.ok(config);
    }
}
