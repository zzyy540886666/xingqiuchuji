package com.xingqiu.server.meta;

import com.xingqiu.server.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetaController {

    @GetMapping("/")
    public ApiResponse<String> root() {
        return ApiResponse.ok("XingQiu Server is running");
    }

    @GetMapping("/meta/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.ok("pong");
    }
}
