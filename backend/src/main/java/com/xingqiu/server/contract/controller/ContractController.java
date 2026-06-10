package com.xingqiu.server.contract.controller;

import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.contract.service.ContractService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping("/{orderId}/contract/download-url")
    public ApiResponse<Map<String, Object>> getContractDownloadUrl(@PathVariable Long orderId) {
        Map<String, Object> result = contractService.getDownloadUrl(orderId, currentUserId());
        return ApiResponse.ok(result);
    }

    private Long currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        throw new IllegalStateException("No authenticated user found");
    }
}
