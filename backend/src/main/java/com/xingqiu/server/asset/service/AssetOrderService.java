package com.xingqiu.server.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.asset.domain.UserAsset;
import com.xingqiu.server.asset.mapper.AssetMapper;
import com.xingqiu.server.catalog.domain.Sku;
import com.xingqiu.server.catalog.service.SkuService;
import com.xingqiu.server.order.domain.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AssetOrderService {

    private static final Logger log = LoggerFactory.getLogger(AssetOrderService.class);

    private final AssetMapper assetMapper;
    private final SkuService skuService;

    public AssetOrderService(AssetMapper assetMapper, SkuService skuService) {
        this.assetMapper = assetMapper;
        this.skuService = skuService;
    }

    @Transactional
    public void grantPurchasedAsset(Order order) {
        if (!"BUY".equalsIgnoreCase(order.getOrderType())) {
            return;
        }

        String purchaseOrderId = order.getId().toString();
        LambdaQueryWrapper<UserAsset> existingQuery = new LambdaQueryWrapper<>();
        existingQuery.eq(UserAsset::getUserId, order.getUserId())
                .eq(UserAsset::getPurchaseOrderId, purchaseOrderId);
        if (assetMapper.selectCount(existingQuery) > 0) {
            log.info("asset already granted for orderId={}", order.getId());
            return;
        }

        Sku sku = skuService.getSkuById(order.getSkuId());
        LocalDateTime now = LocalDateTime.now();

        UserAsset asset = new UserAsset();
        asset.setUserId(order.getUserId());
        asset.setName(sku.getName());
        asset.setType("ROBOT");
        asset.setModelInfo(sku.getName());
        asset.setImageUrl("");
        asset.setStatus("IDLE");
        asset.setPurchaseOrderId(purchaseOrderId);
        asset.setAcquiredAt(now);
        asset.setCreatedAt(now);
        assetMapper.insert(asset);

        log.info("asset granted for paid BUY order, orderId={}, assetId={}", order.getId(), asset.getId());
    }
}
