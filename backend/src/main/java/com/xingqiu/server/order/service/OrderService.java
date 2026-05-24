package com.xingqiu.server.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.catalog.domain.Sku;
import com.xingqiu.server.catalog.service.SkuService;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.order.domain.*;
import com.xingqiu.server.order.dto.CreateOrderRequest;
import com.xingqiu.server.order.dto.OrderResponse;
import com.xingqiu.server.order.dto.PreviewRequest;
import com.xingqiu.server.order.dto.PreviewResponse;
import com.xingqiu.server.order.mapper.OrderEventMapper;
import com.xingqiu.server.order.mapper.OrderLineMapper;
import com.xingqiu.server.order.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderMapper orderMapper;
    private final OrderLineMapper orderLineMapper;
    private final OrderEventMapper orderEventMapper;
    private final OrderDomainService orderDomainService;
    private final PreviewService previewService;
    private final SkuService skuService;

    public OrderService(OrderMapper orderMapper,
                        OrderLineMapper orderLineMapper,
                        OrderEventMapper orderEventMapper,
                        OrderDomainService orderDomainService,
                        PreviewService previewService,
                        SkuService skuService) {
        this.orderMapper = orderMapper;
        this.orderLineMapper = orderLineMapper;
        this.orderEventMapper = orderEventMapper;
        this.orderDomainService = orderDomainService;
        this.previewService = previewService;
        this.skuService = skuService;
    }

    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        // 1. Validate SKU exists and can sell
        Sku sku = skuService.getSkuById(request.getSkuId());

        // 2. Calculate price via preview service
        PreviewRequest previewReq = new PreviewRequest();
        previewReq.setSkuId(request.getSkuId());
        previewReq.setOrderType(request.getOrderType());
        previewReq.setRentStartDate(request.getRentStartDate());
        previewReq.setRentEndDate(request.getRentEndDate());
        previewReq.setAddress(request.getAddress());
        PreviewResponse preview = previewService.preview(previewReq);

        // 3. Generate order number
        String orderNo = generateOrderNo();

        // 4. Create order
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setSkuId(sku.getId());
        order.setOrderType(request.getOrderType());
        order.setStatus(OrderStatus.PENDING_PAY);
        order.setAmountMinor(preview.getPayableAmount());
        order.setDepositMinor(preview.getDepositAmount());
        order.setShippingMinor(0L);
        order.setDiscountMinor(0L);
        order.setPayableMinor(preview.getPayableAmount());
        order.setRentStartDate(request.getRentStartDate());
        order.setRentEndDate(request.getRentEndDate());
        order.setAddressJson(request.getAddress());
        order.setIdempotencyKey(request.getIdempotencyKey());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        orderMapper.insert(order);

        // 5. Create order line
        OrderLine line = new OrderLine();
        line.setOrderId(order.getId());
        line.setSkuId(sku.getId());
        line.setSkuName(sku.getName());
        line.setQuantity(1);
        line.setUnitPriceMinor(preview.getPayableAmount());
        orderLineMapper.insert(line);

        // 6. Log creation event
        logEvent(order.getId(), null, OrderStatus.PENDING_PAY.name(), "SYSTEM", "订单创建");

        log.info("Order {} created for user {}, payable={} cents", orderNo, userId, preview.getPayableAmount());

        return toResponse(order);
    }

    public OrderResponse getOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权查看此订单");
        }
        return toResponse(order);
    }

    public PageResult<OrderResponse> listOrders(Long userId, String status, String type, int page, int pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);

        if (type != null && !type.isBlank()) {
            String normalizedType = type.toUpperCase();
            if (!normalizedType.equals("RENT") && !normalizedType.equals("BUY") && !normalizedType.equals("SOFTWARE")) {
                throw new BizException(ErrorCode.BAD_REQUEST, "无效的订单类型: " + type);
            }
            wrapper.eq(Order::getOrderType, normalizedType);
        }

        if (status != null && !status.isBlank()) {
            try {
                wrapper.eq(Order::getStatus, OrderStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BizException(ErrorCode.BAD_REQUEST, "无效的订单状态: " + status);
            }
        }

        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> pageObj = new Page<>(page, pageSize);
        Page<Order> result = orderMapper.selectPage(pageObj, wrapper);

        var items = result.getRecords().stream()
                .map(this::toResponse)
                .toList();

        return PageResult.of(items, result.getTotal(), page, pageSize);
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }

        if (!order.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权操作此订单");
        }

        OrderStatus before = order.getStatus();
        orderDomainService.transition(order, OrderStatus.CANCELLED, "USER");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        logEvent(orderId, before.name(), OrderStatus.CANCELLED.name(), "USER", "用户取消订单");

        log.info("Order {} cancelled by user {}", order.getOrderNo(), userId);
    }

    public Order getOrderEntity(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    @Transactional
    public void updateStatus(Order order, OrderStatus target, String operator) {
        orderDomainService.transition(order, target, operator);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    private void logEvent(Long orderId, String fromStatus, String toStatus, String operator, String reason) {
        OrderEvent event = new OrderEvent();
        event.setOrderId(orderId);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setOperator(operator);
        event.setReason(reason);
        event.setCreatedAt(LocalDateTime.now());
        orderEventMapper.insert(event);
    }

    private String generateOrderNo() {
        return "XQ" + System.currentTimeMillis()
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse resp = new OrderResponse();
        resp.setId(order.getId());
        resp.setOrderNo(order.getOrderNo());
        resp.setUserId(order.getUserId());
        resp.setSkuId(order.getSkuId());
        resp.setOrderType(order.getOrderType());
        resp.setStatus(order.getStatus().name());
        resp.setAmountMinor(order.getAmountMinor());
        resp.setDepositMinor(order.getDepositMinor());
        resp.setShippingMinor(order.getShippingMinor());
        resp.setDiscountMinor(order.getDiscountMinor());
        resp.setPayableMinor(order.getPayableMinor());
        resp.setRentStartDate(order.getRentStartDate());
        resp.setRentEndDate(order.getRentEndDate());
        resp.setAddressJson(order.getAddressJson());
        resp.setIdempotencyKey(order.getIdempotencyKey());
        resp.setCreatedAt(order.getCreatedAt());
        resp.setUpdatedAt(order.getUpdatedAt());
        return resp;
    }
}
