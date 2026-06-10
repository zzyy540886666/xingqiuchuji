package com.xingqiu.server.order.service;

import com.xingqiu.server.catalog.domain.Sku;
import com.xingqiu.server.catalog.domain.SkuPrice;
import com.xingqiu.server.catalog.service.SkuPriceService;
import com.xingqiu.server.catalog.service.SkuService;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.order.dto.PreviewRequest;
import com.xingqiu.server.order.dto.PreviewResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreviewServiceTest {

    @Mock
    private SkuService skuService;

    @Mock
    private SkuPriceService skuPriceService;

    private PreviewService previewService;

    @BeforeEach
    void setUp() {
        previewService = new PreviewService(skuService, skuPriceService);
    }

    @Test
    void previewRentUsesMatchedDurationDailyRateAndDeposit() {
        Sku sku = sku(1L, Sku.SkuType.RENT);
        when(skuService.getSkuById(1L)).thenReturn(sku);
        when(skuPriceService.getBySkuId(1L)).thenReturn(List.of(
                price("DAILY_RENT", 200000L, 1, 29, 12000L),
                price("DAILY_RENT", 200000L, 30, 365, 9000L)
        ));

        PreviewRequest request = request("RENT");
        request.setRentStartDate(LocalDate.of(2026, 6, 1));
        request.setRentEndDate(LocalDate.of(2026, 7, 1));

        PreviewResponse response = previewService.preview(request);

        assertThat(response.getDepositAmount()).isEqualTo(200000L);
        assertThat(response.getDiscountAmount()).isZero();
        assertThat(response.getPayableAmount()).isEqualTo(470000L);
    }

    @Test
    void previewBuyUsesBuyPriceOnlyWithoutDeposit() {
        Sku sku = sku(2L, Sku.SkuType.BUY);
        when(skuService.getSkuById(1L)).thenReturn(sku);
        when(skuPriceService.getBySkuId(2L)).thenReturn(List.of(
                price("DAILY_RENT", 200000L, 1, 365, 10000L),
                price("BUY", 8800000L, null, null, null)
        ));

        PreviewResponse response = previewService.preview(request("BUY"));

        assertThat(response.getDepositAmount()).isZero();
        assertThat(response.getPayableAmount()).isEqualTo(8800000L);
    }

    @Test
    void previewSoftwareUsesSubscriptionPriceOnlyWithoutRentOrDeposit() {
        Sku sku = sku(3L, Sku.SkuType.SOFTWARE);
        when(skuService.getSkuById(1L)).thenReturn(sku);
        when(skuPriceService.getBySkuId(3L)).thenReturn(List.of(
                price("SUBSCRIPTION", 199900L, null, null, null)
        ));

        PreviewResponse response = previewService.preview(request("SOFTWARE"));

        assertThat(response.getDepositAmount()).isZero();
        assertThat(response.getPayableAmount()).isEqualTo(199900L);
    }

    @Test
    void previewRejectsClientSubmittedPositiveDiscounts() {
        Sku sku = sku(2L, Sku.SkuType.BUY);
        when(skuService.getSkuById(1L)).thenReturn(sku);
        when(skuPriceService.getBySkuId(2L)).thenReturn(List.of(price("BUY", 10000L, null, null, null)));

        PreviewRequest request = request("BUY");
        request.setCouponDiscountMinor(7000L);
        request.setLightYearDiscountMinor(8000L);

        assertThatThrownBy(() -> previewService.preview(request))
                .isInstanceOf(BizException.class);
    }

    @Test
    void previewRentRejectsMissingRentDates() {
        Sku sku = sku(1L, Sku.SkuType.RENT);
        when(skuService.getSkuById(1L)).thenReturn(sku);
        when(skuPriceService.getBySkuId(1L)).thenReturn(List.of(price("DAILY_RENT", 200000L, 1, 365, 10000L)));

        assertThatThrownBy(() -> previewService.preview(request("RENT")))
                .isInstanceOf(BizException.class);
    }

    private PreviewRequest request(String orderType) {
        PreviewRequest request = new PreviewRequest();
        request.setSkuId(1L);
        request.setOrderType(orderType);
        return request;
    }

    private Sku sku(Long id, Sku.SkuType type) {
        Sku sku = new Sku();
        sku.setId(id);
        sku.setType(type);
        sku.setStatus(Sku.SkuStatus.ONLINE);
        return sku;
    }

    private SkuPrice price(String priceType, Long priceMinor, Integer minDuration, Integer maxDuration, Long dailyRateMinor) {
        SkuPrice price = new SkuPrice();
        price.setPriceType(priceType);
        price.setPriceMinor(priceMinor);
        price.setMinDuration(minDuration);
        price.setMaxDuration(maxDuration);
        price.setDailyRateMinor(dailyRateMinor);
        return price;
    }
}
