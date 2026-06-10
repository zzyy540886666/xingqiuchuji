package com.xingqiu.server.member.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.member.domain.Membership;
import com.xingqiu.server.member.domain.MembershipBenefitGrant;
import com.xingqiu.server.member.domain.MembershipEvent;
import com.xingqiu.server.member.domain.PlanetCardSku;
import com.xingqiu.server.member.dto.MembershipResponse;
import com.xingqiu.server.member.mapper.MembershipBenefitGrantMapper;
import com.xingqiu.server.member.mapper.MembershipEventMapper;
import com.xingqiu.server.member.mapper.MembershipMapper;
import com.xingqiu.server.member.mapper.PlanetCardMapper;
import com.xingqiu.server.appconfig.service.ConfigService;
import com.xingqiu.server.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    /** 等级升级阈值（单位：分，硬编码兜底）:
     *  L0: < 10000  (即累计消费 < 100 元)
     *  L1: >= 10000 (累计消费 >= 100 元)
     *  L2: >= 50000 (累计消费 >= 500 元)
     *  L3: >= 200000 (累计消费 >= 2000 元)
     */
    private static final long DEFAULT_L1_THRESHOLD_CENTS = 10_000L;
    private static final long DEFAULT_L2_THRESHOLD_CENTS = 50_000L;
    private static final long DEFAULT_L3_THRESHOLD_CENTS = 200_000L;

    private final MembershipMapper membershipMapper;
    private final MembershipBenefitGrantMapper benefitGrantMapper;
    private final MembershipEventMapper eventMapper;
    private final PlanetCardMapper planetCardMapper;
    private final WalletService walletService;
    private final ConfigService configService;

    public MemberService(MembershipMapper membershipMapper,
                         MembershipBenefitGrantMapper benefitGrantMapper,
                         MembershipEventMapper eventMapper,
                         PlanetCardMapper planetCardMapper,
                         WalletService walletService,
                         ConfigService configService) {
        this.membershipMapper = membershipMapper;
        this.benefitGrantMapper = benefitGrantMapper;
        this.eventMapper = eventMapper;
        this.planetCardMapper = planetCardMapper;
        this.walletService = walletService;
        this.configService = configService;
    }

    /**
     * 获取用户会员信息，不存在则创建默认记录
     */
    public MembershipResponse getMembership(Long userId) {
        Membership membership = findOrCreate(userId);

        LambdaQueryWrapper<MembershipBenefitGrant> grantQuery = new LambdaQueryWrapper<>();
        grantQuery.eq(MembershipBenefitGrant::getUserId, userId);
        List<MembershipBenefitGrant> benefits = benefitGrantMapper.selectList(grantQuery);

        return toResponse(membership, benefits);
    }

    public List<PlanetCardSku> listPlanetCards() {
        LambdaQueryWrapper<PlanetCardSku> query = new LambdaQueryWrapper<>();
        query.eq(PlanetCardSku::getStatus, "ON_SALE")
                .gt(PlanetCardSku::getStock, 0)
                .orderByAsc(PlanetCardSku::getPriceMinor);
        return planetCardMapper.selectList(query);
    }

    /**
     * 支付成功后重算会员等级
     * 更新 lifetimeSpendMinor，根据阈值判定是否升级
     */
    @Transactional
    public void recalculate(Long userId) {
        Membership membership = findOrCreate(userId);

        int newLevel = deriveLevel(membership.getLifetimeSpendMinor());
        int oldLevel = membership.getLevel();

        if (newLevel > oldLevel) {
            membership.setLevel(newLevel);
            membership.setUpdatedAt(LocalDateTime.now());
            membershipMapper.updateById(membership);

            MembershipEvent event = new MembershipEvent();
            event.setUserId(userId);
            event.setEventType("LEVEL_UP");
            event.setDescription("会员等级升至 L" + newLevel);
            event.setCreatedAt(LocalDateTime.now());
            eventMapper.insert(event);

            log.info("userId={} level upgraded: L{} -> L{}", userId, oldLevel, newLevel);
        }
    }

    /**
     * 首次购买时标记为原生用户
     */
    @Transactional
    public void tagNative(Long userId) {
        Membership membership = findOrCreate(userId);
        if (Boolean.TRUE.equals(membership.getIsNative())) {
            return;
        }
        membership.setIsNative(true);
        membership.setUpdatedAt(LocalDateTime.now());
        membershipMapper.updateById(membership);

        MembershipEvent event = new MembershipEvent();
        event.setUserId(userId);
        event.setEventType("NATIVE_TAGGED");
        event.setDescription("首次购买，标记为原生用户");
        event.setCreatedAt(LocalDateTime.now());
        eventMapper.insert(event);
    }

    /**
     * 购买星球卡
     * 校验库存，更新 planetCardExpiresAt
     */
    @Transactional
    public void purchasePlanetCard(Long userId, Long skuId) {
        PlanetCardSku sku = planetCardMapper.selectById(skuId);
        if (sku == null) {
            throw new BizException(ErrorCode.PLANET_CARD_SOLD_OUT, "星球卡SKU不存在");
        }
        if (!"ON_SALE".equals(sku.getStatus())) {
            throw new BizException(ErrorCode.PLANET_CARD_SOLD_OUT, "星球卡已下架");
        }
        if (sku.getStock() != null && sku.getStock() <= 0) {
            throw new BizException(ErrorCode.PLANET_CARD_SOLD_OUT, "星球卡库存不足");
        }

        // 使用账户余额完成付款，确保会员权益不会在未付款时发放。
        walletService.debit(userId, sku.getPriceMinor(), "PLANET_CARD", sku.getId().toString(),
                "购买星球卡: " + sku.getName());

        // 扣减库存
        sku.setStock(sku.getStock() - 1);
        if (sku.getStock() == 0) {
            sku.setStatus("SOLD_OUT");
        }
        planetCardMapper.updateById(sku);

        // 更新会员星球卡到期时间
        Membership membership = findOrCreate(userId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentExpiry = membership.getPlanetCardExpiresAt();
        if (currentExpiry == null || currentExpiry.isBefore(now)) {
            currentExpiry = now;
        }
        membership.setPlanetCardExpiresAt(currentExpiry.plusDays(sku.getDurationDays()));
        membership.setUpdatedAt(now);
        membershipMapper.updateById(membership);

        MembershipEvent event = new MembershipEvent();
        event.setUserId(userId);
        event.setEventType("PLANET_CARD_PURCHASE");
        event.setDescription("购买星球卡: " + sku.getName());
        event.setCreatedAt(now);
        eventMapper.insert(event);

        log.info("userId={} purchased planetCard skuId={}", userId, skuId);
    }

    @Transactional
    public void recordPaidOrder(Long userId, Long amountMinor, Long orderId) {
        if (amountMinor == null || amountMinor < 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Invalid paid amount");
        }
        if (hasPaidOrderEvent(userId, orderId)) {
            log.info("membership paid order already recorded, userId={}, orderId={}", userId, orderId);
            return;
        }

        Membership membership = findOrCreate(userId);
        int oldLevel = membership.getLevel();
        boolean wasNative = Boolean.TRUE.equals(membership.getIsNative());

        membership.setLifetimeSpendMinor(membership.getLifetimeSpendMinor() + amountMinor);
        membership.setIsNative(true);
        int newLevel = deriveLevel(membership.getLifetimeSpendMinor());
        if (newLevel > oldLevel) {
            membership.setLevel(newLevel);
        }
        membership.setUpdatedAt(LocalDateTime.now());
        membershipMapper.updateById(membership);

        MembershipEvent paidEvent = new MembershipEvent();
        paidEvent.setUserId(userId);
        paidEvent.setEventType("PAID_ORDER");
        paidEvent.setDescription(paidOrderDescription(orderId));
        paidEvent.setCreatedAt(LocalDateTime.now());
        eventMapper.insert(paidEvent);

        if (!wasNative) {
            MembershipEvent nativeEvent = new MembershipEvent();
            nativeEvent.setUserId(userId);
            nativeEvent.setEventType("NATIVE_TAGGED");
            nativeEvent.setDescription("First paid order tagged native");
            nativeEvent.setCreatedAt(LocalDateTime.now());
            eventMapper.insert(nativeEvent);
        }

        if (newLevel > oldLevel) {
            MembershipEvent levelEvent = new MembershipEvent();
            levelEvent.setUserId(userId);
            levelEvent.setEventType("LEVEL_UP");
            levelEvent.setDescription("Membership level upgraded to L" + newLevel);
            levelEvent.setCreatedAt(LocalDateTime.now());
            eventMapper.insert(levelEvent);
            log.info("userId={} level upgraded: L{} -> L{}", userId, oldLevel, newLevel);
        }
    }

    // ---- internal helpers ----

    private Membership findOrCreate(Long userId) {
        LambdaQueryWrapper<Membership> query = new LambdaQueryWrapper<>();
        query.eq(Membership::getUserId, userId);
        Membership membership = membershipMapper.selectOne(query);
        if (membership == null) {
            membership = Membership.createDefault(userId);
            membershipMapper.insert(membership);
        }
        return membership;
    }

    private boolean hasPaidOrderEvent(Long userId, Long orderId) {
        LambdaQueryWrapper<MembershipEvent> query = new LambdaQueryWrapper<>();
        query.eq(MembershipEvent::getUserId, userId)
                .eq(MembershipEvent::getEventType, "PAID_ORDER")
                .eq(MembershipEvent::getDescription, paidOrderDescription(orderId));
        Long count = eventMapper.selectCount(query);
        return count != null && count > 0;
    }

    private String paidOrderDescription(Long orderId) {
        return "orderId=" + orderId;
    }

    private int deriveLevel(long lifetimeSpendMinor) {
        long l1 = DEFAULT_L1_THRESHOLD_CENTS;
        long l2 = DEFAULT_L2_THRESHOLD_CENTS;
        long l3 = DEFAULT_L3_THRESHOLD_CENTS;

        try {
            List<java.util.Map<String, Object>> rules = configService.getMembershipRules();
            if (rules != null && !rules.isEmpty()) {
                l1 = readThreshold(rules, 1, DEFAULT_L1_THRESHOLD_CENTS);
                l2 = readThreshold(rules, 2, DEFAULT_L2_THRESHOLD_CENTS);
                l3 = readThreshold(rules, 3, DEFAULT_L3_THRESHOLD_CENTS);
            }
        } catch (Exception e) {
            log.warn("Failed to load membership rules from config, using defaults: {}", e.getMessage());
        }

        if (lifetimeSpendMinor >= l3) {
            return 3;
        }
        if (lifetimeSpendMinor >= l2) {
            return 2;
        }
        if (lifetimeSpendMinor >= l1) {
            return 1;
        }
        return 0;
    }

    private long readThreshold(List<java.util.Map<String, Object>> rules, int level, long defaultMinor) {
        for (java.util.Map<String, Object> rule : rules) {
            Object levelObj = rule.get("level");
            int ruleLevel = -1;
            if (levelObj instanceof Integer) {
                ruleLevel = (Integer) levelObj;
            } else if (levelObj instanceof Number) {
                ruleLevel = ((Number) levelObj).intValue();
            }
            if (ruleLevel == level) {
                Object thresholdObj = rule.get("thresholdMinor");
                if (thresholdObj instanceof Long) {
                    return (Long) thresholdObj;
                } else if (thresholdObj instanceof Integer) {
                    return ((Integer) thresholdObj).longValue();
                } else if (thresholdObj instanceof Number) {
                    return ((Number) thresholdObj).longValue();
                }
            }
        }
        return defaultMinor;
    }

    private MembershipResponse toResponse(Membership m, List<MembershipBenefitGrant> benefits) {
        MembershipResponse resp = new MembershipResponse();
        resp.setLevel(m.getLevel());
        resp.setIsNative(m.getIsNative());
        resp.setLifetimeSpendMinor(m.getLifetimeSpendMinor());
        resp.setPlanetCardExpiresAt(m.getPlanetCardExpiresAt());
        resp.setBenefits(benefits);
        return resp;
    }
}
