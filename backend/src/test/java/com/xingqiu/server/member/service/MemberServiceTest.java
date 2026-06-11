package com.xingqiu.server.member.service;

import com.xingqiu.server.appconfig.service.ConfigService;
import com.xingqiu.server.member.domain.Membership;
import com.xingqiu.server.member.mapper.MembershipBenefitGrantMapper;
import com.xingqiu.server.member.mapper.MembershipEventMapper;
import com.xingqiu.server.member.mapper.MembershipMapper;
import com.xingqiu.server.member.mapper.PlanetCardMapper;
import com.xingqiu.server.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MembershipMapper membershipMapper;
    @Mock
    private MembershipBenefitGrantMapper benefitGrantMapper;
    @Mock
    private MembershipEventMapper eventMapper;
    @Mock
    private PlanetCardMapper planetCardMapper;
    @Mock
    private WalletService walletService;
    @Mock
    private ConfigService configService;

    private AtomicReference<Membership> storedMembership;
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        storedMembership = new AtomicReference<>();
        when(membershipMapper.selectOne(any())).thenAnswer(invocation -> storedMembership.get());
        when(membershipMapper.insert(any(Membership.class))).thenAnswer(invocation -> {
            storedMembership.set(invocation.getArgument(0));
            return 1;
        });
        when(membershipMapper.updateById(any(Membership.class))).thenAnswer(invocation -> {
            storedMembership.set(invocation.getArgument(0));
            return 1;
        });
        when(eventMapper.selectCount(any())).thenReturn(0L);
        when(configService.getMembershipRules()).thenReturn(defaultRules());

        memberService = new MemberService(
                membershipMapper,
                benefitGrantMapper,
                eventMapper,
                planetCardMapper,
                walletService,
                configService
        );
    }

    @Test
    void firstPositivePaidOrderPromotesToLevelOneAndNative() {
        memberService.recordPaidOrder(10L, 1L, 100L);

        Membership membership = storedMembership.get();
        assertThat(membership.getLevel()).isEqualTo(1);
        assertThat(membership.getIsNative()).isTrue();
        assertThat(membership.getLifetimeSpendMinor()).isEqualTo(1L);
    }

    @Test
    void zeroAmountPaidOrderDoesNotGrantFirstOrderMembership() {
        memberService.recordPaidOrder(10L, 0L, 100L);

        Membership membership = storedMembership.get();
        assertThat(membership.getLevel()).isZero();
        assertThat(membership.getIsNative()).isFalse();
        assertThat(membership.getLifetimeSpendMinor()).isZero();
    }

    @Test
    void cumulativeSpendAtFiveThousandYuanPromotesToLevelTwo() {
        memberService.recordPaidOrder(10L, 500_000L, 100L);

        Membership membership = storedMembership.get();
        assertThat(membership.getLevel()).isEqualTo(2);
        assertThat(membership.getLifetimeSpendMinor()).isEqualTo(500_000L);
    }

    @Test
    void cumulativeSpendAtTwentyThousandYuanPromotesToLevelThree() {
        memberService.recordPaidOrder(10L, 2_000_000L, 100L);

        Membership membership = storedMembership.get();
        assertThat(membership.getLevel()).isEqualTo(3);
        assertThat(membership.getLifetimeSpendMinor()).isEqualTo(2_000_000L);
    }

    @Test
    void configuredThresholdMinorTakesPrecedenceOverDefaultRules() {
        when(configService.getMembershipRules()).thenReturn(List.of(
                rule(1, 9_900L),
                rule(2, 1_000L),
                rule(3, 10_000_000L)
        ));

        memberService.recordPaidOrder(10L, 1_000L, 100L);

        Membership membership = storedMembership.get();
        assertThat(membership.getLevel()).isEqualTo(2);
        assertThat(membership.getLifetimeSpendMinor()).isEqualTo(1_000L);
    }

    private List<Map<String, Object>> defaultRules() {
        return List.of(
                rule(1, 9_900L),
                rule(2, 500_000L),
                rule(3, 2_000_000L)
        );
    }

    private Map<String, Object> rule(int level, long thresholdMinor) {
        return Map.of(
                "level", level,
                "thresholdMinor", thresholdMinor
        );
    }
}
