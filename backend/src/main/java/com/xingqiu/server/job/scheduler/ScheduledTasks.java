package com.xingqiu.server.job.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.distribution.service.CommissionService;
import com.xingqiu.server.order.domain.Order;
import com.xingqiu.server.order.domain.OrderStatus;
import com.xingqiu.server.order.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private final StringRedisTemplate redis;
    private final OrderMapper orderMapper;
    private final CommissionService commissionService;

    public ScheduledTasks(StringRedisTemplate redis,
                          OrderMapper orderMapper,
                          CommissionService commissionService) {
        this.redis = redis;
        this.orderMapper = orderMapper;
        this.commissionService = commissionService;
    }

    /**
     * Cancel expired pending-pay orders every 60 seconds.
     * Uses Redis distributed lock (SET NX EX) to prevent concurrent execution.
     */
    @Scheduled(fixedDelay = 60_000)
    public void cancelExpiredOrders() {
        String lockKey = "lock:cancel-orders";

        Boolean acquired = redis.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(30));
        if (Boolean.FALSE.equals(acquired)) {
            log.debug("cancelExpiredOrders: lock not acquired, skipping");
            return;
        }

        try {
            LocalDateTime thirtyMinAgo = LocalDateTime.now().minusMinutes(30);

            LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<>();
            query.eq(Order::getStatus, OrderStatus.PENDING_PAY)
                    .lt(Order::getCreatedAt, thirtyMinAgo);

            List<Order> expiredOrders = orderMapper.selectList(query);

            if (expiredOrders.isEmpty()) {
                log.debug("No expired orders to cancel");
                return;
            }

            int cancelled = 0;
            for (Order order : expiredOrders) {
                order.setStatus(OrderStatus.CANCELLED);
                order.setUpdatedAt(LocalDateTime.now());
                orderMapper.updateById(order);
                cancelled++;
                log.info("Cancelled expired order: orderId={}, orderNo={}, created={}",
                        order.getId(), order.getOrderNo(), order.getCreatedAt());
            }

            log.info("cancelExpiredOrders: cancelled {} orders", cancelled);
        } catch (Exception e) {
            log.error("cancelExpiredOrders failed: {}", e.getMessage(), e);
        }
        // Lock auto-expires after 30 seconds; no explicit unlock needed.
    }

    /**
     * Settle commissions daily at 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void settleCommissions() {
        String lockKey = "lock:settle-commissions";

        Boolean acquired = redis.opsForValue().setIfAbsent(lockKey, "1", Duration.ofMinutes(5));
        if (Boolean.FALSE.equals(acquired)) {
            log.info("settleCommissions: lock not acquired, skipping");
            return;
        }

        try {
            log.info("Starting daily commission settlement");
            commissionService.settleCommissions();
            log.info("Daily commission settlement completed");
        } catch (Exception e) {
            log.error("settleCommissions failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Inspect trusteeship — placeholder that runs every 5 minutes.
     */
    @Scheduled(fixedDelay = 300_000)
    public void inspectTrusteeship() {
        log.info("Trusteeship inspection run — placeholder");
    }
}
