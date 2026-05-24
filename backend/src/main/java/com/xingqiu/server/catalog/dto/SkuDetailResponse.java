package com.xingqiu.server.catalog.dto;

import com.xingqiu.server.catalog.domain.SkuPrice;
import com.xingqiu.server.catalog.domain.SkuMedia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SkuDetailResponse {

    private Long id;
    private String name;
    private String title;
    private String type;
    private Long brandId;
    private String brand;
    private Long modelId;
    private String description;
    private String specsJson;
    private Map<String, String> specs;
    private String subtitle;
    private Long originalPriceMinor;
    private String adaptedScenesText;
    private String stockStatusText;
    private String deliveryText;
    private String status;
    private Integer stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String image;
    private List<String> media;
    private List<SkuMedia> mediaItems;
    private Long priceAmount;
    private List<SkuPrice> prices;
    private List<String> tags;
    private List<Map<String, Object>> services;
    private List<Map<String, Object>> detailSections;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Long getModelId() { return modelId; }
    public void setModelId(Long modelId) { this.modelId = modelId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSpecsJson() { return specsJson; }
    public void setSpecsJson(String specsJson) { this.specsJson = specsJson; }

    public Map<String, String> getSpecs() { return specs; }
    public void setSpecs(Map<String, String> specs) { this.specs = specs; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public Long getOriginalPriceMinor() { return originalPriceMinor; }
    public void setOriginalPriceMinor(Long originalPriceMinor) { this.originalPriceMinor = originalPriceMinor; }

    public String getAdaptedScenesText() { return adaptedScenesText; }
    public void setAdaptedScenesText(String adaptedScenesText) { this.adaptedScenesText = adaptedScenesText; }

    public String getStockStatusText() { return stockStatusText; }
    public void setStockStatusText(String stockStatusText) { this.stockStatusText = stockStatusText; }

    public String getDeliveryText() { return deliveryText; }
    public void setDeliveryText(String deliveryText) { this.deliveryText = deliveryText; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public List<String> getMedia() { return media; }
    public void setMedia(List<String> media) { this.media = media; }

    public List<SkuMedia> getMediaItems() { return mediaItems; }
    public void setMediaItems(List<SkuMedia> mediaItems) { this.mediaItems = mediaItems; }

    public Long getPriceAmount() { return priceAmount; }
    public void setPriceAmount(Long priceAmount) { this.priceAmount = priceAmount; }

    public List<SkuPrice> getPrices() { return prices; }
    public void setPrices(List<SkuPrice> prices) { this.prices = prices; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<Map<String, Object>> getServices() { return services; }
    public void setServices(List<Map<String, Object>> services) { this.services = services; }

    public List<Map<String, Object>> getDetailSections() { return detailSections; }
    public void setDetailSections(List<Map<String, Object>> detailSections) { this.detailSections = detailSections; }
}
