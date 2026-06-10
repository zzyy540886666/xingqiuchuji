package com.xingqiu.server.contract.service;

import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.contract.adapter.CosAdapter;
import com.xingqiu.server.contract.adapter.PdfGenerator;
import com.xingqiu.server.contract.domain.Contract;
import com.xingqiu.server.contract.mapper.ContractMapper;
import com.xingqiu.server.order.domain.Order;
import com.xingqiu.server.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContractServiceTest {

    private ContractMapper contractMapper;
    private CosAdapter cosAdapter;
    private OrderService orderService;
    private ContractService contractService;

    @BeforeEach
    void setUp() {
        contractMapper = mock(ContractMapper.class);
        cosAdapter = mock(CosAdapter.class);
        orderService = mock(OrderService.class);
        contractService = new ContractService(contractMapper, mock(PdfGenerator.class), cosAdapter, orderService);
    }

    @Test
    void getDownloadUrlReturnsPresignedUrlForOrderOwner() {
        Long orderId = 10L;
        Long userId = 99L;
        when(orderService.getOrderEntity(orderId)).thenReturn(order(orderId, userId));
        when(contractMapper.selectOne(any())).thenReturn(contract(orderId, "contracts/XQ10.pdf"));
        when(cosAdapter.generatePresignedUrl("contracts/XQ10.pdf", 3600L))
                .thenReturn("https://cos.example/contracts/XQ10.pdf?sign=1");

        Map<String, Object> result = contractService.getDownloadUrl(orderId, userId);

        assertThat(result).containsEntry("url", "https://cos.example/contracts/XQ10.pdf?sign=1");
        assertThat(result.get("expiresAt")).isInstanceOf(Long.class);
    }

    @Test
    void getDownloadUrlRejectsNonOwnerBeforeReadingContract() {
        Long orderId = 10L;
        when(orderService.getOrderEntity(orderId)).thenReturn(order(orderId, 99L));

        assertThatThrownBy(() -> contractService.getDownloadUrl(orderId, 100L))
                .isInstanceOf(BizException.class)
                .extracting(ex -> ((BizException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(contractMapper, never()).selectOne(any());
        verify(cosAdapter, never()).generatePresignedUrl(any(), any(Long.class));
    }

    private Order order(Long orderId, Long userId) {
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        return order;
    }

    private Contract contract(Long orderId, String cosKey) {
        Contract contract = new Contract();
        contract.setOrderId(orderId);
        contract.setCosKey(cosKey);
        return contract;
    }
}
