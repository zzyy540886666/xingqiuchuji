package com.xingqiu.server.order.service;

import com.xingqiu.server.catalog.domain.Sku;
import com.xingqiu.server.catalog.domain.SkuPrice;
import com.xingqiu.server.catalog.service.SkuPriceService;
import com.xingqiu.server.catalog.service.SkuService;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.order.dto.PreviewRequest;
import com.xingqiu.server.order.dto.PreviewResponse;
import com.xingqiu.server.order.dto.PreviewResponse.PriceBreakdownItem;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class PreviewService {

    private static final Long DEFAULT_DEPOSIT_MINOR = 200000L;

    private final SkuService skuService;
    private final SkuPriceService skuPriceService;

    public PreviewService(SkuService skuService,
                          SkuPriceService skuPriceService) {
        this.skuService = skuService;
        this.skuPriceService = skuPriceService;
    }

    public PreviewResponse preview(PreviewRequest request) {
        String orderType = normalizeOrderType(request.getOrderType());
        Sku sku = skuService.getSkuById(request.getSkuId());
        List<SkuPrice> prices = skuPriceService.getBySkuId(sku.getId());

        PreviewResponse base = switch (orderType) {
            case "RENT" -> previewRent(request, prices);
            case "BUY" -> previewBuy(sku, prices);
            case "SOFTWARE" -> previewSoftware(sku, prices);
            default -> throw new BizException(ErrorCode.BAD_REQUEST, "Invalid order type: " + request.getOrderType());
        };
        return applyDiscount(base, request);
    }

    public String normalizeOrderType(String orderType) {
        if (orderType == null || orderType.isBlank()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Order type is required");
        }
        String normalized = orderType.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("RENT") && !normalized.equals("BUY") && !normalized.equals("SOFTWARE")) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Invalid order type: " + orderType);
        }
        return normalized;
    }

    private PreviewResponse previewRent(PreviewRequest request, List<SkuPrice> prices) {
        long rentDays = rentDays(request.getRentStartDate(), request.getRentEndDate());
        SkuPrice matchedPrice = findBestRentPrice(prices, (int) rentDays);
        if (matchedPrice == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Rent price is not configured");
        }

        long dailyRateMinor = money(matchedPrice.getDailyRateMinor());
        long rentalCostMinor = dailyRateMinor * rentDays;
        long depositMinor = matchedPrice.getPriceMinor() != null ? matchedPrice.getPriceMinor() : DEFAULT_DEPOSIT_MINOR;
        long shippingMinor = 0L;

        List<PriceBreakdownItem> breakdown = new ArrayList<>();
        breakdown.add(new PriceBreakdownItem("Daily rent", dailyRateMinor));
        breakdown.add(new PriceBreakdownItem("Rent days", rentDays));
        breakdown.add(new PriceBreakdownItem("Rent subtotal", rentalCostMinor));
        breakdown.add(new PriceBreakdownItem("Deposit", depositMinor));
        breakdown.add(new PriceBreakdownItem("Shipping", shippingMinor));

        return response(breakdown, rentalCostMinor + depositMinor + shippingMinor, depositMinor, 0L);
    }

    private PreviewResponse previewBuy(Sku sku, List<SkuPrice> prices) {
        SkuPrice matchedPrice = findFixedPrice(sku, prices, "BUY");
        if (matchedPrice == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Buy price is not configured");
        }
        long buyoutMinor = money(matchedPrice.getPriceMinor());
        List<PriceBreakdownItem> breakdown = new ArrayList<>();
        breakdown.add(new PriceBreakdownItem("Buyout price", buyoutMinor));
        breakdown.add(new PriceBreakdownItem("Deposit", 0L));
        breakdown.add(new PriceBreakdownItem("Shipping", 0L));
        return response(breakdown, buyoutMinor, 0L, 0L);
    }

    private PreviewResponse previewSoftware(Sku sku, List<SkuPrice> prices) {
        SkuPrice matchedPrice = findSoftwarePrice(sku, prices);
        if (matchedPrice == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Software price is not configured");
        }
        long licenseMinor = money(matchedPrice.getPriceMinor());
        List<PriceBreakdownItem> breakdown = new ArrayList<>();
        breakdown.add(new PriceBreakdownItem("Software license", licenseMinor));
        breakdown.add(new PriceBreakdownItem("Deposit", 0L));
        return response(breakdown, licenseMinor, 0L, 0L);
    }

    private PreviewResponse applyDiscount(PreviewResponse base, PreviewRequest request) {
        if (positive(request.getCouponDiscountMinor()) || positive(request.getLightYearDiscountMinor())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "客户端提交的优惠金额不可信，请通过服务端优惠券/余额接口核销");
        }
        base.setDiscountAmount(0L);
        return base;
    }

    private PreviewResponse response(List<PriceBreakdownItem> breakdown, long payableMinor, long depositMinor, long discountMinor) {
        PreviewResponse response = new PreviewResponse();
        response.setPriceBreakdown(breakdown);
        response.setPayableAmount(payableMinor);
        response.setDepositAmount(depositMinor);
        response.setDiscountAmount(discountMinor);
        return response;
    }

    private long rentDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Rent period is required");
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Rent end date must be after start date");
        }
        return days;
    }

    private SkuPrice findBestRentPrice(List<SkuPrice> prices, int days) {
        return prices.stream()
                .filter(p -> isPriceType(p, "DAILY_RENT", "RENT", "LEASE_BUY") || prices.size() == 1)
                .filter(p -> p.getMinDuration() != null && p.getMaxDuration() != null)
                .filter(p -> days >= p.getMinDuration() && days <= p.getMaxDuration())
                .min(Comparator.comparingLong(p -> money(p.getDailyRateMinor())))
                .orElse(null);
    }

    private SkuPrice findFixedPrice(Sku sku, List<SkuPrice> prices, String priceType) {
        return prices.stream()
                .filter(p -> isPriceType(p, priceType) || isSingleMatchingSkuPrice(sku, prices, priceType))
                .findFirst()
                .orElse(null);
    }

    private SkuPrice findSoftwarePrice(Sku sku, List<SkuPrice> prices) {
        return prices.stream()
                .filter(p -> isPriceType(p, "SOFTWARE", "SUBSCRIPTION", "LICENSE", "AUTHORIZATION")
                        || isSingleMatchingSkuPrice(sku, prices, "SOFTWARE"))
                .findFirst()
                .orElse(null);
    }

    private boolean isSingleMatchingSkuPrice(Sku sku, List<SkuPrice> prices, String orderType) {
        return prices.size() == 1
                && sku.getType() != null
                && sku.getType().name().equals(orderType);
    }

    private boolean isPriceType(SkuPrice price, String... expectedTypes) {
        if (price.getPriceType() == null) {
            return false;
        }
        String actual = price.getPriceType().trim().toUpperCase(Locale.ROOT);
        for (String expected : expectedTypes) {
            if (actual.equals(expected)) {
                return true;
            }
        }
        return false;
    }

    private static long money(Long value) {
        return value == null ? 0L : value;
    }

    private static boolean positive(Long value) {
        return value != null && value > 0;
    }
}
