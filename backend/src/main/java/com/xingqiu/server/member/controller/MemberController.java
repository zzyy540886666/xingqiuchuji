package com.xingqiu.server.member.controller;

import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.member.dto.MembershipResponse;
import com.xingqiu.server.member.service.MemberService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 获取当前用户会员信息
     */
    @GetMapping("/users/me/membership")
    public ApiResponse<MembershipResponse> getMembership() {
        Long userId = currentUserId();
        MembershipResponse resp = memberService.getMembership(userId);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/membership/planet-cards")
    public ApiResponse<List<com.xingqiu.server.member.domain.PlanetCardSku>> getPlanetCards() {
        return ApiResponse.ok(memberService.listPlanetCards());
    }

    /**
     * 购买星球卡
     */
    @PostMapping("/membership/planet-cards/{skuId}/purchase")
    public ApiResponse<Void> purchasePlanetCard(@PathVariable Long skuId) {
        Long userId = currentUserId();
        memberService.purchasePlanetCard(userId, skuId);
        return ApiResponse.ok(null);
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
