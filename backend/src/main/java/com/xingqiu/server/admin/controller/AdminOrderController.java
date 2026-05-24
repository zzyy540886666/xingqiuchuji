package com.xingqiu.server.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.order.domain.Order;
import com.xingqiu.server.order.domain.OrderStatus;
import com.xingqiu.server.order.mapper.OrderMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {

    private final OrderMapper orderMapper;

    public AdminOrderController(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @GetMapping
    public ApiResponse<PageResult<Order>> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            try {
                query.eq(Order::getStatus, OrderStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BizException(ErrorCode.BAD_REQUEST, "无效的订单状态: " + status);
            }
        }
        if (userId != null) {
            query.eq(Order::getUserId, userId);
        }
        query.orderByDesc(Order::getCreatedAt);

        Page<Order> pageObj = new Page<>(page, pageSize);
        Page<Order> result = orderMapper.selectPage(pageObj, query);
        return ApiResponse.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }
}
