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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PreviewService {

    private static final Logger log = LoggerFactory.getLogger(PreviewService.class);

    /** Default deposit in cents (e.g., 200000 = 2000.00 CNY). */
    private static final Long DEFAULT_DEPOSIT_MINOR = 200000L;

    private final SkuService skuService;
    private final SkuPriceService skuPriceService;

    public PreviewService(SkuService skuService,
                          SkuPriceService skuPriceService) {
        this.skuService = skuService;
        this.skuPriceService = skuPriceService;
    }

    /**
     * Calculate order price preview using Long math only.
     * Formula: dailyRateMinor * days + deposit
     */
    public PreviewResponse preview(PreviewRequest request) {
        // 1. Validate SKU exists and is online
        Sku sku = skuService.getSkuById(request.getSkuId());

        // 2. Get SKU prices
        List<SkuPrice> prices = skuPriceService.getBySkuId(sku.getId());

        // 3. Find best matching price for the requested duration
        long rentDays = 1;
        if (request.getRentStartDate() != null && request.getRentEndDate() != null) {
            rentDays = ChronoUnit.DAYS.between(request.getRentStartDate(), request.getRentEndDate());
            if (rentDays <= 0) {
                rentDays = 1;
            }
        }

        // Find most suitable price entry by duration
        SkuPrice matchedPrice = findBestPrice(prices, (int) rentDays);
        if (matchedPrice == null && !prices.isEmpty()) {
            matchedPrice = prices.get(0);
        }

        Long dailyRateMinor = matchedPrice != null && matchedPrice.getDailyRateMinor() != null
                ? matchedPrice.getDailyRateMinor() : 0L;

        // 4. Calculate: dailyRate * days (Long math only)
        Long rentalCostMinor = dailyRateMinor * rentDays;

        // 5. Deposit (use price's deposit or default)
        Long depositMinor = matchedPrice != null && matchedPrice.getPriceMinor() != null
                ? matchedPrice.getPriceMinor() : DEFAULT_DEPOSIT_MINOR;

        // 6. Build breakdown
        List<PriceBreakdownItem> breakdown = new ArrayList<>();
        breakdown.add(new PriceBreakdownItem("日租金", dailyRateMinor));
        breakdown.add(new PriceBreakdownItem("租期天数", rentDays));
        breakdown.add(new PriceBreakdownItem("租金小计", rentalCostMinor));
        breakdown.add(new PriceBreakdownItem("押金", depositMinor));

        // Shipping (0 for now — to be implemented with shipping module)
        Long shippingMinor = 0L;
        breakdown.add(new PriceBreakdownItem("运费", shippingMinor));

        Long payableMinor = rentalCostMinor + depositMinor + shippingMinor;

        PreviewResponse response = new PreviewResponse();
        response.setPriceBreakdown(breakdown);
        response.setPayableAmount(payableMinor);
        response.setDepositAmount(depositMinor);

        return response;
    }

    private SkuPrice findBestPrice(List<SkuPrice> prices, int days) {
        return prices.stream()
                .filter(p -> p.getMinDuration() != null && p.getMaxDuration() != null)
                .filter(p -> days >= p.getMinDuration() && days <= p.getMaxDuration())
                .min(Comparator.comparingLong(SkuPrice::getDailyRateMinor))
                .orElse(null);
    }
}
