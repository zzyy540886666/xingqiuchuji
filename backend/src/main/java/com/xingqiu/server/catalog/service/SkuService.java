package com.xingqiu.server.catalog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingqiu.server.catalog.domain.Sku;
import com.xingqiu.server.catalog.domain.Brand;
import com.xingqiu.server.catalog.domain.Sku.SkuStatus;
import com.xingqiu.server.catalog.domain.Sku.SkuType;
import com.xingqiu.server.catalog.domain.SkuDetailSection;
import com.xingqiu.server.catalog.domain.SkuMedia;
import com.xingqiu.server.catalog.domain.SkuPrice;
import com.xingqiu.server.catalog.domain.SkuServiceItem;
import com.xingqiu.server.catalog.domain.SkuTag;
import com.xingqiu.server.catalog.dto.SkuDetailResponse;
import com.xingqiu.server.catalog.dto.SkuListRequest;
import com.xingqiu.server.catalog.mapper.SkuDetailSectionMapper;
import com.xingqiu.server.catalog.mapper.BrandMapper;
import com.xingqiu.server.catalog.mapper.SkuMapper;
import com.xingqiu.server.catalog.mapper.SkuServiceItemMapper;
import com.xingqiu.server.catalog.mapper.SkuTagMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
public class SkuService {

    private static final Logger log = LoggerFactory.getLogger(SkuService.class);

    private final SkuMapper skuMapper;
    private final BrandMapper brandMapper;
    private final SkuMediaService skuMediaService;
    private final SkuPriceService skuPriceService;
    private final SkuTagMapper skuTagMapper;
    private final SkuServiceItemMapper skuServiceItemMapper;
    private final SkuDetailSectionMapper skuDetailSectionMapper;
    private final ObjectMapper objectMapper;

    public SkuService(SkuMapper skuMapper,
                      BrandMapper brandMapper,
                      SkuMediaService skuMediaService,
                      SkuPriceService skuPriceService,
                      SkuTagMapper skuTagMapper,
                      SkuServiceItemMapper skuServiceItemMapper,
                      SkuDetailSectionMapper skuDetailSectionMapper,
                      ObjectMapper objectMapper) {
        this.skuMapper = skuMapper;
        this.brandMapper = brandMapper;
        this.skuMediaService = skuMediaService;
        this.skuPriceService = skuPriceService;
        this.skuTagMapper = skuTagMapper;
        this.skuServiceItemMapper = skuServiceItemMapper;
        this.skuDetailSectionMapper = skuDetailSectionMapper;
        this.objectMapper = objectMapper;
    }

    @Cacheable(value = "skus", key = "'list_' + #request.type + '_' + #request.brandId + '_' + #request.minPrice + '_' + #request.maxPrice + '_' + #request.modelId + '_' + #request.q + '_' + #request.page + '_' + #request.pageSize")
    public PageResult<SkuDetailResponse> list(SkuListRequest request) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Sku::getStatus, SkuStatus.ONLINE);

        if (request.getType() != null && !request.getType().isBlank()) {
            try {
                wrapper.eq(Sku::getType, SkuType.valueOf(request.getType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid type filter
            }
        }

        if (request.getBrandId() != null) {
            wrapper.eq(Sku::getBrandId, request.getBrandId());
        }

        if (request.getModelId() != null) {
            wrapper.eq(Sku::getModelId, request.getModelId());
        }

        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            LambdaQueryWrapper<SkuPrice> priceWrapper = new LambdaQueryWrapper<>();
            if (request.getMinPrice() != null) {
                priceWrapper.ge(SkuPrice::getPriceMinor, request.getMinPrice());
            }
            if (request.getMaxPrice() != null) {
                priceWrapper.lt(SkuPrice::getPriceMinor, request.getMaxPrice());
            }
            List<Long> skuIds = skuPriceService.findSkuIds(priceWrapper);
            if (skuIds.isEmpty()) {
                return PageResult.of(List.of(), 0L, request.getPage(), request.getPageSize());
            }
            wrapper.in(Sku::getId, skuIds);
        }

        if (request.getQ() != null && !request.getQ().isBlank()) {
            String keyword = request.getQ().trim();
            List<Long> relatedSkuIds = findKeywordSkuIds(keyword);
            wrapper.and(w -> {
                w.like(Sku::getName, keyword)
                        .or().like(Sku::getDescription, keyword)
                        .or().like(Sku::getSubtitle, keyword)
                        .or().like(Sku::getAdaptedScenesText, keyword)
                        .or().like(Sku::getSpecsJson, keyword);
                if (!relatedSkuIds.isEmpty()) {
                    w.or().in(Sku::getId, relatedSkuIds);
                }
            });
        }

        wrapper.orderByDesc(Sku::getUpdatedAt);

        Page<Sku> page = new Page<>(request.getPage(), request.getPageSize());
        Page<Sku> result = skuMapper.selectPage(page, wrapper);

        List<SkuDetailResponse> records = result.getRecords().stream().map(this::toDetail).toList();
        return PageResult.of(records, result.getTotal(), request.getPage(), request.getPageSize());
    }

    @Cacheable(value = "skus", key = "#skuId")
    public SkuDetailResponse getById(Long skuId) {
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BizException(ErrorCode.SKU_NOT_FOUND);
        }
        if (sku.getStatus() == SkuStatus.OFFLINE) {
            throw new BizException(ErrorCode.SKU_OFFLINE);
        }
        return toDetail(sku);
    }

    public Sku getSkuById(Long skuId) {
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BizException(ErrorCode.SKU_NOT_FOUND);
        }
        if (sku.getStatus() == SkuStatus.OFFLINE) {
            throw new BizException(ErrorCode.SKU_OFFLINE);
        }
        return sku;
    }

    private SkuDetailResponse toDetail(Sku sku) {
        SkuDetailResponse detail = new SkuDetailResponse();
        detail.setId(sku.getId());
        detail.setName(sku.getName());
        detail.setTitle(sku.getName());
        detail.setType(sku.getType().name());
        detail.setBrandId(sku.getBrandId());
        detail.setBrand(brandName(sku.getBrandId()));
        detail.setModelId(sku.getModelId());
        detail.setDescription(sku.getDescription());
        detail.setSpecsJson(sku.getSpecsJson());
        detail.setSpecs(parseSpecs(sku.getSpecsJson()));
        detail.setSubtitle(sku.getSubtitle());
        detail.setOriginalPriceMinor(sku.getOriginalPriceMinor());
        detail.setAdaptedScenesText(sku.getAdaptedScenesText());
        detail.setStockStatusText(sku.getStockStatusText());
        detail.setDeliveryText(sku.getDeliveryText());
        detail.setStatus(sku.getStatus().name());
        detail.setStock(sku.getStock());
        detail.setCreatedAt(sku.getCreatedAt());
        detail.setUpdatedAt(sku.getUpdatedAt());

        List<SkuMedia> media = skuMediaService.getBySkuId(sku.getId());
        List<String> mediaUrls = media.stream().map(SkuMedia::getUrl).filter(url -> url != null && !url.isBlank()).toList();
        detail.setMedia(mediaUrls);
        detail.setMediaItems(media);
        detail.setImage(mediaUrls.isEmpty() ? "" : mediaUrls.get(0));

        List<SkuPrice> prices = skuPriceService.getBySkuId(sku.getId());
        detail.setPrices(prices);
        detail.setPriceAmount(prices.isEmpty() ? 0L : prices.get(0).getPriceMinor());
        detail.setTags(loadTags(sku.getId()));
        detail.setServices(loadServices(sku.getId()));
        detail.setDetailSections(loadDetailSections(sku.getId()));

        return detail;
    }

    private Map<String, String> parseSpecs(String specsJson) {
        if (specsJson == null || specsJson.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(specsJson, new TypeReference<LinkedHashMap<String, String>>() {});
        } catch (Exception e) {
            Map<String, String> fallback = new LinkedHashMap<>();
            fallback.put("说明", specsJson);
            return fallback;
        }
    }

    private List<String> loadTags(Long skuId) {
        return skuTagMapper.selectList(new LambdaQueryWrapper<SkuTag>()
                        .eq(SkuTag::getSkuId, skuId)
                        .orderByAsc(SkuTag::getSortOrder))
                .stream()
                .map(SkuTag::getName)
                .toList();
    }

    private List<Map<String, Object>> loadServices(Long skuId) {
        return skuServiceItemMapper.selectList(new LambdaQueryWrapper<SkuServiceItem>()
                        .eq(SkuServiceItem::getSkuId, skuId)
                        .orderByAsc(SkuServiceItem::getSortOrder))
                .stream()
                .map(item -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", item.getName());
                    row.put("price", item.getPriceLabel());
                    row.put("priceLabel", item.getPriceLabel());
                    row.put("sortOrder", item.getSortOrder());
                    return row;
                })
                .toList();
    }

    private List<Map<String, Object>> loadDetailSections(Long skuId) {
        return skuDetailSectionMapper.selectList(new LambdaQueryWrapper<SkuDetailSection>()
                        .eq(SkuDetailSection::getSkuId, skuId)
                        .orderByAsc(SkuDetailSection::getSortOrder))
                .stream()
                .map(section -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("title", section.getTitle());
                    row.put("content", section.getContent());
                    row.put("sortOrder", section.getSortOrder());
                    return row;
                })
                .toList();
    }

    private String brandName(Long brandId) {
        Brand brand = brandId == null ? null : brandMapper.selectById(brandId);
        return brand == null ? "" : brand.getName();
    }

    private List<Long> findKeywordSkuIds(String keyword) {
        Set<Long> skuIds = new LinkedHashSet<>();
        skuTagMapper.selectList(new LambdaQueryWrapper<SkuTag>().like(SkuTag::getName, keyword))
                .forEach(tag -> skuIds.add(tag.getSkuId()));
        skuServiceItemMapper.selectList(new LambdaQueryWrapper<SkuServiceItem>().like(SkuServiceItem::getName, keyword))
                .forEach(service -> skuIds.add(service.getSkuId()));
        skuDetailSectionMapper.selectList(new LambdaQueryWrapper<SkuDetailSection>()
                        .like(SkuDetailSection::getTitle, keyword)
                        .or()
                        .like(SkuDetailSection::getContent, keyword))
                .forEach(section -> skuIds.add(section.getSkuId()));
        return List.copyOf(skuIds);
    }
}
