package com.xingqiu.server.order.controller;

import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.order.dto.CreateOrderRequest;
import com.xingqiu.server.order.dto.OrderResponse;
import com.xingqiu.server.order.dto.PreviewRequest;
import com.xingqiu.server.order.dto.PreviewResponse;
import com.xingqiu.server.order.service.OrderService;
import com.xingqiu.server.order.service.PreviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
public class OrderController {

    private final OrderService orderService;
    private final PreviewService previewService;

    public OrderController(OrderService orderService,
                           PreviewService previewService) {
        this.orderService = orderService;
        this.previewService = previewService;
    }

    @PostMapping("/preview")
    public ApiResponse<PreviewResponse> preview(@Valid @RequestBody PreviewRequest request) {
        PreviewResponse response = previewService.preview(request);
        return ApiResponse.ok(response);
    }

    @PostMapping
    public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        Long userId = getCurrentUserId();
        OrderResponse response = orderService.createOrder(userId, request);
        return ApiResponse.ok(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        OrderResponse response = orderService.getOrder(id, userId);
        return ApiResponse.ok(response);
    }

    @GetMapping
    public ApiResponse<PageResult<OrderResponse>> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {
        Long userId = getCurrentUserId();
        PageResult<OrderResponse> result = orderService.listOrders(userId, status, type, page, pageSize);
        return ApiResponse.ok(result);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        orderService.cancelOrder(id, userId);
        return ApiResponse.ok(null);
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new IllegalStateException("No authenticated user found");
    }
}
