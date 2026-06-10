package com.xingqiu.server.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingqiu.server.appconfig.service.CatalogPlacementService;
import com.xingqiu.server.catalog.domain.Sku;
import com.xingqiu.server.catalog.domain.Brand;
import com.xingqiu.server.catalog.domain.SkuDetailSection;
import com.xingqiu.server.catalog.domain.SkuMedia;
import com.xingqiu.server.catalog.domain.SkuPrice;
import com.xingqiu.server.catalog.domain.SkuServiceItem;
import com.xingqiu.server.catalog.domain.SkuTag;
import com.xingqiu.server.catalog.mapper.SkuDetailSectionMapper;
import com.xingqiu.server.catalog.mapper.BrandMapper;
import com.xingqiu.server.catalog.mapper.SkuMapper;
import com.xingqiu.server.catalog.mapper.SkuMediaMapper;
import com.xingqiu.server.catalog.mapper.SkuPriceMapper;
import com.xingqiu.server.catalog.mapper.SkuServiceItemMapper;
import com.xingqiu.server.catalog.mapper.SkuTagMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin/skus")
public class AdminSkuController {

    private final SkuMapper skuMapper;
    private final BrandMapper brandMapper;
    private final SkuMediaMapper skuMediaMapper;
    private final SkuPriceMapper skuPriceMapper;
    private final SkuTagMapper skuTagMapper;
    private final SkuServiceItemMapper skuServiceItemMapper;
    private final SkuDetailSectionMapper skuDetailSectionMapper;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;
    private final CatalogPlacementService catalogPlacementService;

    public AdminSkuController(SkuMapper skuMapper, BrandMapper brandMapper, SkuMediaMapper skuMediaMapper,
                              SkuPriceMapper skuPriceMapper,
                              SkuTagMapper skuTagMapper,
                              SkuServiceItemMapper skuServiceItemMapper,
                              SkuDetailSectionMapper skuDetailSectionMapper,
                              ObjectMapper objectMapper,
                              CacheManager cacheManager,
                              CatalogPlacementService catalogPlacementService) {
        this.skuMapper = skuMapper;
        this.brandMapper = brandMapper;
        this.skuMediaMapper = skuMediaMapper;
        this.skuPriceMapper = skuPriceMapper;
        this.skuTagMapper = skuTagMapper;
        this.skuServiceItemMapper = skuServiceItemMapper;
        this.skuDetailSectionMapper = skuDetailSectionMapper;
        this.objectMapper = objectMapper;
        this.cacheManager = cacheManager;
        this.catalogPlacementService = catalogPlacementService;
    }

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String term = keyword.trim();
            List<Long> brandIds = brandMapper.selectList(new LambdaQueryWrapper<Brand>().like(Brand::getName, term))
                    .stream().map(Brand::getId).toList();
            wrapper.and(query -> {
                query.like(Sku::getName, term)
                        .or().like(Sku::getDescription, term)
                        .or().like(Sku::getSubtitle, term);
                if (!brandIds.isEmpty()) {
                    query.or().in(Sku::getBrandId, brandIds);
                }
            });
        }
        if (type != null && !type.isBlank()) {
            try {
                wrapper.eq(Sku::getType, Sku.SkuType.valueOf(type));
            } catch (IllegalArgumentException ex) {
                throw new BizException(ErrorCode.BAD_REQUEST, "Invalid SKU type: " + type);
            }
        }
        Sku.SkuStatus skuStatus = toSkuStatus(status);
        if (skuStatus != null) {
            wrapper.eq(Sku::getStatus, skuStatus);
        }
        wrapper.orderByDesc(Sku::getCreatedAt);

        Page<Sku> p = skuMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Set<Long> recommendedIds = catalogPlacementService.getRecommendedSkuIds();
        List<Map<String, Object>> items = p.getRecords().stream().map(sku -> toRow(sku, recommendedIds)).toList();
        return ApiResponse.ok(PageResult.of(items, p.getTotal(), page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable Long id) {
        Sku sku = requireSku(id);
        Map<String, Object> row = toRow(sku);
        row.put("specs", sku.getSpecsJson());
        row.put("specsJson", sku.getSpecsJson());
        row.put("models", sku.getModelId() == null ? "" : String.valueOf(sku.getModelId()));
        row.put("description", sku.getDescription());
        row.put("media", skuMediaMapper.selectList(new LambdaQueryWrapper<SkuMedia>().eq(SkuMedia::getSkuId, id).orderByAsc(SkuMedia::getSortOrder)));
        row.put("prices", skuPriceMapper.selectList(new LambdaQueryWrapper<SkuPrice>().eq(SkuPrice::getSkuId, id)));
        row.put("tags", skuTagMapper.selectList(new LambdaQueryWrapper<SkuTag>().eq(SkuTag::getSkuId, id).orderByAsc(SkuTag::getSortOrder)).stream().map(SkuTag::getName).toList());
        row.put("services", skuServiceItemMapper.selectList(new LambdaQueryWrapper<SkuServiceItem>().eq(SkuServiceItem::getSkuId, id).orderByAsc(SkuServiceItem::getSortOrder)));
        row.put("detailSections", skuDetailSectionMapper.selectList(new LambdaQueryWrapper<SkuDetailSection>().eq(SkuDetailSection::getSkuId, id).orderByAsc(SkuDetailSection::getSortOrder)));
        return ApiResponse.ok(row);
    }

    @PostMapping
    @Transactional
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Sku sku = new Sku();
        applyBody(sku, body);
        sku.setStatus(Sku.SkuStatus.OFFLINE);
        sku.setCreatedAt(LocalDateTime.now());
        sku.setUpdatedAt(LocalDateTime.now());
        skuMapper.insert(sku);
        saveDetailData(sku.getId(), body);
        clearSkuCache();
        return ApiResponse.ok(toRow(requireSku(sku.getId())));
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Sku sku = requireSku(id);
        applyBody(sku, body);
        sku.setUpdatedAt(LocalDateTime.now());
        skuMapper.updateById(sku);
        saveDetailData(id, body);
        clearSkuCache();
        return ApiResponse.ok(toRow(requireSku(id)));
    }

    @PostMapping("/{id}/toggle-shelf")
    @Transactional
    public ApiResponse<Void> toggleShelf(@PathVariable Long id) {
        Sku sku = requireSku(id);
        sku.setStatus(sku.getStatus() == Sku.SkuStatus.ONLINE ? Sku.SkuStatus.OFFLINE : Sku.SkuStatus.ONLINE);
        sku.setUpdatedAt(LocalDateTime.now());
        skuMapper.updateById(sku);
        clearSkuCache();
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/toggle-recommend")
    @Transactional
    public ApiResponse<Map<String, Object>> toggleRecommend(@PathVariable Long id) {
        requireSku(id);
        boolean recommended = catalogPlacementService.toggleRecommendedSku(id);
        clearSkuCache();
        return ApiResponse.ok(Map.of("recommended", recommended));
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Sku sku = requireSku(id);
        sku.setStatus(toSkuStatus(body.get("status")));
        sku.setUpdatedAt(LocalDateTime.now());
        skuMapper.updateById(sku);
        clearSkuCache();
        return ApiResponse.ok(null);
    }

    private Sku requireSku(Long id) {
        Sku sku = skuMapper.selectById(id);
        if (sku == null) {
            throw new BizException(ErrorCode.SKU_NOT_FOUND);
        }
        return sku;
    }

    private void applyBody(Sku sku, Map<String, Object> body) {
        sku.setName(asString(body.getOrDefault("name", body.get("title"))));
        String type = asString(body.getOrDefault("type", "RENT"));
        sku.setType(Sku.SkuType.valueOf(type));
        sku.setBrandId(number(body.get("brandId"), 1L));
        sku.setModelId(number(body.get("modelId"), sku.getModelId()));
        sku.setDescription(asString(body.getOrDefault("description", body.get("specs"))));
        sku.setSpecsJson(jsonOrString(body.getOrDefault("specsJson", body.get("specs"))));
        sku.setSubtitle(asString(body.get("subtitle")));
        sku.setOriginalPriceMinor(number(body.get("originalPriceMinor"), sku.getOriginalPriceMinor()));
        sku.setAdaptedScenesText(asString(body.get("adaptedScenesText")));
        sku.setStockStatusText(asString(body.get("stockStatusText")));
        sku.setDeliveryText(asString(body.get("deliveryText")));
        sku.setStock(number(body.get("stock"), 0L).intValue());
    }

    private void saveDetailData(Long skuId, Map<String, Object> body) {
        savePrices(skuId, body);
        saveMedia(skuId, body);
        if (body.containsKey("tags")) saveTags(skuId, body.get("tags"));
        if (body.containsKey("services")) saveServices(skuId, listBody(body.get("services")));
        if (body.containsKey("detailSections")) saveSections(skuId, listBody(body.get("detailSections")));
    }

    private Map<String, Object> toRow(Sku sku) {
        return toRow(sku, catalogPlacementService.getRecommendedSkuIds());
    }

    private Map<String, Object> toRow(Sku sku, Set<Long> recommendedIds) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(sku.getId()));
        row.put("name", sku.getName());
        row.put("type", sku.getType() == null ? "RENT" : sku.getType().name());
        row.put("brandId", sku.getBrandId());
        row.put("brand", brandName(sku.getBrandId()));
        row.put("description", sku.getDescription());
        row.put("subtitle", sku.getSubtitle());
        row.put("originalPriceMinor", sku.getOriginalPriceMinor());
        row.put("adaptedScenesText", sku.getAdaptedScenesText());
        row.put("stockStatusText", sku.getStockStatusText());
        row.put("deliveryText", sku.getDeliveryText());
        row.put("status", sku.getStatus() == Sku.SkuStatus.ONLINE ? "ON_SHELF" : "OFF_SHELF");
        row.put("stock", sku.getStock());
        row.put("createdAt", sku.getCreatedAt());
        row.put("updatedAt", sku.getUpdatedAt());
        row.put("priceFen", firstPrice(sku.getId()));
        row.put("coverImage", firstImage(sku.getId()));
        row.put("recommended", recommendedIds.contains(sku.getId()));
        return row;
    }

    private void savePrices(Long skuId, Map<String, Object> body) {
        List<Map<String, Object>> prices = listBody(body.get("prices"));
        Object price = body.containsKey("priceMinor") ? body.get("priceMinor") : body.get("priceFen");
        if (prices.isEmpty() && price == null) {
            return;
        }
        skuPriceMapper.delete(new LambdaQueryWrapper<SkuPrice>().eq(SkuPrice::getSkuId, skuId));
        if (!prices.isEmpty()) {
            for (Map<String, Object> item : prices) {
                SkuPrice p = new SkuPrice();
                p.setSkuId(skuId);
                p.setPriceType(asString(item.getOrDefault("priceType", "DAILY_RENT")));
                p.setPriceMinor(number(item.get("priceMinor"), 0L));
                p.setDailyRateMinor(number(item.get("dailyRateMinor"), p.getPriceMinor()));
                p.setMinDuration(number(item.get("minDuration"), 1L).intValue());
                p.setMaxDuration(number(item.get("maxDuration"), 365L).intValue());
                skuPriceMapper.insert(p);
            }
            return;
        }
        SkuPrice p = new SkuPrice();
        p.setSkuId(skuId);
        p.setPriceType("DAILY_RENT");
        p.setPriceMinor(number(price, 0L));
        p.setDailyRateMinor(p.getPriceMinor());
        p.setMinDuration(1);
        p.setMaxDuration(365);
        skuPriceMapper.insert(p);
    }

    private void saveMedia(Long skuId, Map<String, Object> body) {
        List<Map<String, Object>> mediaRows = listBody(body.get("media"));
        String cover = asString(body.getOrDefault("coverImage", body.get("imageUrl")));
        if (mediaRows.isEmpty() && (cover == null || cover.isBlank())) {
            return;
        }
        skuMediaMapper.delete(new LambdaQueryWrapper<SkuMedia>().eq(SkuMedia::getSkuId, skuId));
        if (!mediaRows.isEmpty()) {
            int index = 0;
            for (Map<String, Object> item : mediaRows) {
                String url = asString(item.get("url"));
                if (url == null || url.isBlank()) continue;
                SkuMedia media = new SkuMedia();
                media.setSkuId(skuId);
                media.setUrl(url);
                media.setType(SkuMedia.MediaType.valueOf(asString(item.getOrDefault("type", "IMAGE"))));
                media.setSortOrder(number(item.get("sortOrder"), (long) index).intValue());
                skuMediaMapper.insert(media);
                index++;
            }
            return;
        }
        SkuMedia media = new SkuMedia();
        media.setSkuId(skuId);
        media.setUrl(cover);
        media.setType(SkuMedia.MediaType.IMAGE);
        media.setSortOrder(0);
        skuMediaMapper.insert(media);
    }

    private void saveTags(Long skuId, Object tagsBody) {
        if (tagsBody == null) return;
        skuTagMapper.delete(new LambdaQueryWrapper<SkuTag>().eq(SkuTag::getSkuId, skuId));
        List<String> tags;
        if (tagsBody instanceof List<?> list) {
            tags = list.stream().map(String::valueOf).toList();
        } else {
            tags = List.of(String.valueOf(tagsBody).split(","));
        }
        int index = 0;
        for (String tagName : tags) {
            String name = tagName.trim();
            if (name.isBlank()) continue;
            SkuTag tag = new SkuTag();
            tag.setSkuId(skuId);
            tag.setName(name);
            tag.setSortOrder(index++);
            skuTagMapper.insert(tag);
        }
    }

    private void saveServices(Long skuId, List<Map<String, Object>> services) {
        skuServiceItemMapper.delete(new LambdaQueryWrapper<SkuServiceItem>().eq(SkuServiceItem::getSkuId, skuId));
        int index = 0;
        for (Map<String, Object> item : services) {
            SkuServiceItem service = new SkuServiceItem();
            service.setSkuId(skuId);
            service.setName(asString(item.get("name")));
            service.setPriceLabel(asString(item.getOrDefault("priceLabel", item.getOrDefault("price", "免费"))));
            service.setSortOrder(number(item.get("sortOrder"), (long) index).intValue());
            skuServiceItemMapper.insert(service);
            index++;
        }
    }

    private void saveSections(Long skuId, List<Map<String, Object>> sections) {
        skuDetailSectionMapper.delete(new LambdaQueryWrapper<SkuDetailSection>().eq(SkuDetailSection::getSkuId, skuId));
        int index = 0;
        for (Map<String, Object> item : sections) {
            SkuDetailSection section = new SkuDetailSection();
            section.setSkuId(skuId);
            section.setTitle(asString(item.get("title")));
            section.setContent(asString(item.get("content")));
            section.setSortOrder(number(item.get("sortOrder"), (long) index).intValue());
            skuDetailSectionMapper.insert(section);
            index++;
        }
    }

    private Long firstPrice(Long skuId) {
        List<SkuPrice> prices = skuPriceMapper.selectPage(new Page<SkuPrice>(1, 1),
                new LambdaQueryWrapper<SkuPrice>().eq(SkuPrice::getSkuId, skuId)).getRecords();
        return prices.isEmpty() ? 0L : prices.get(0).getPriceMinor();
    }

    private String firstImage(Long skuId) {
        List<SkuMedia> media = skuMediaMapper.selectPage(new Page<SkuMedia>(1, 1),
                new LambdaQueryWrapper<SkuMedia>()
                        .eq(SkuMedia::getSkuId, skuId)
                        .orderByAsc(SkuMedia::getSortOrder)).getRecords();
        return media.isEmpty() ? "" : media.get(0).getUrl();
    }

    private Sku.SkuStatus toSkuStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return switch (status) {
            case "ON_SHELF", "ONLINE" -> Sku.SkuStatus.ONLINE;
            case "OFF_SHELF", "DRAFT", "OFFLINE" -> Sku.SkuStatus.OFFLINE;
            default -> throw new BizException(ErrorCode.BAD_REQUEST, "Invalid SKU status: " + status);
        };
    }

    private String brandName(Long brandId) {
        Brand brand = brandId == null ? null : brandMapper.selectById(brandId);
        return brand == null ? "" : brand.getName();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String jsonOrString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String s) {
            return s;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Invalid JSON field");
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listBody(Object value) {
        if (value instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return objectMapper.readValue(s, new TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception e) {
                throw new BizException(ErrorCode.BAD_REQUEST, "Invalid JSON array");
            }
        }
        return List.of();
    }

    private Long number(Object value, Long fallback) {
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private void clearSkuCache() {
        if (cacheManager.getCache("skus") != null) {
            cacheManager.getCache("skus").clear();
        }
    }
}
