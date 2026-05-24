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

    /** 等级升级阈值（单位：分）:
     *  L0: < 10000  (即累计消费 < 100 元)
     *  L1: >= 10000 (累计消费 >= 100 元)
     *  L2: >= 50000 (累计消费 >= 500 元)
     *  L3: >= 200000 (累计消费 >= 2000 元)
     */
    private static final long L1_THRESHOLD_CENTS = 10_000L;
    private static final long L2_THRESHOLD_CENTS = 50_000L;
    private static final long L3_THRESHOLD_CENTS = 200_000L;

    private final MembershipMapper membershipMapper;
    private final MembershipBenefitGrantMapper benefitGrantMapper;
    private final MembershipEventMapper eventMapper;
    private final PlanetCardMapper planetCardMapper;
    private final WalletService walletService;

    public MemberService(MembershipMapper membershipMapper,
                         MembershipBenefitGrantMapper benefitGrantMapper,
                         MembershipEventMapper eventMapper,
                         PlanetCardMapper planetCardMapper,
                         WalletService walletService) {
        this.membershipMapper = membershipMapper;
        this.benefitGrantMapper = benefitGrantMapper;
        this.eventMapper = eventMapper;
        this.planetCardMapper = planetCardMapper;
        this.walletService = walletService;
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

    private int deriveLevel(long lifetimeSpendMinor) {
        if (lifetimeSpendMinor >= L3_THRESHOLD_CENTS) {
            return 3;
        }
        if (lifetimeSpendMinor >= L2_THRESHOLD_CENTS) {
            return 2;
        }
        if (lifetimeSpendMinor >= L1_THRESHOLD_CENTS) {
            return 1;
        }
        return 0;
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
