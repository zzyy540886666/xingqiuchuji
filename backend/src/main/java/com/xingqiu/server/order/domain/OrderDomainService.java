package com.xingqiu.server.order.domain;

import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class OrderDomainService {

    private static final Logger log = LoggerFactory.getLogger(OrderDomainService.class);

    /**
     * Valid state transitions:
     * PENDING_PAY -> PAID, CANCELLED
     * PAID -> FULFILLING
     * FULFILLING -> COMPLETED
     * any -> REFUNDING -> REFUNDED
     */
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.PENDING_PAY, Set.of(OrderStatus.PAID, OrderStatus.CANCELLED),
            OrderStatus.PAID, Set.of(OrderStatus.FULFILLING),
            OrderStatus.FULFILLING, Set.of(OrderStatus.COMPLETED)
    );

    /**
     * Validate and execute an order status transition.
     * REFUNDING can be entered from any non-terminal status.
     * REFUNDED can only be entered from REFUNDING.
     */
    public void transition(Order order, OrderStatus target, String operator) {
        OrderStatus current = order.getStatus();

        // Allow refund flow from any active status
        if (target == OrderStatus.REFUNDING) {
            if (current == OrderStatus.REFUNDING || current == OrderStatus.REFUNDED) {
                throw new BizException(ErrorCode.ORDER_STATUS_INVALID,
                        "订单已在退款流程中: " + current);
            }
            log.info("Order {} transitioning from {} to REFUNDING by {}",
                    order.getOrderNo(), current, operator);
            order.setStatus(OrderStatus.REFUNDING);
            return;
        }

        if (target == OrderStatus.REFUNDED) {
            if (current != OrderStatus.REFUNDING) {
                throw new BizException(ErrorCode.ORDER_STATUS_INVALID,
                        "只有REFUNDING状态的订单可转为REFUNDED，当前: " + current);
            }
            log.info("Order {} transitioning from REFUNDING to REFUNDED by {}",
                    order.getOrderNo(), operator);
            order.setStatus(OrderStatus.REFUNDED);
            return;
        }

        // Validate standard transitions
        Set<OrderStatus> allowed = VALID_TRANSITIONS.get(current);
        if (allowed == null || !allowed.contains(target)) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID,
                    "订单状态不可从 " + current + " 迁移到 " + target);
        }

        log.info("Order {} transitioning from {} to {} by {}",
                order.getOrderNo(), current, target, operator);
        order.setStatus(target);
    }
}
