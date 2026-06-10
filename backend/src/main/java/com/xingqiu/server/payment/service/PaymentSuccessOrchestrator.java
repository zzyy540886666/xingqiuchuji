package com.xingqiu.server.payment.service;

import com.xingqiu.server.asset.service.AssetOrderService;
import com.xingqiu.server.contract.service.ContractService;
import com.xingqiu.server.distribution.service.CommissionService;
import com.xingqiu.server.member.service.MemberService;
import com.xingqiu.server.order.domain.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentSuccessOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(PaymentSuccessOrchestrator.class);

    private final ContractService contractService;
    private final MemberService memberService;
    private final AssetOrderService assetOrderService;
    private final CommissionService commissionService;

    public PaymentSuccessOrchestrator(ContractService contractService,
                                      MemberService memberService,
                                      AssetOrderService assetOrderService,
                                      CommissionService commissionService) {
        this.contractService = contractService;
        this.memberService = memberService;
        this.assetOrderService = assetOrderService;
        this.commissionService = commissionService;
    }

    @Transactional
    public void orchestrate(Order order) {
        contractService.generateContract(order.getId());
        memberService.recordPaidOrder(order.getUserId(), order.getPayableMinor(), order.getId());
        assetOrderService.grantPurchasedAsset(order);
        commissionService.calculateCommission(order.getId(), order.getUserId(), order.getPayableMinor());

        log.info("payment success orchestration completed, orderId={}, userId={}", order.getId(), order.getUserId());
    }
}
