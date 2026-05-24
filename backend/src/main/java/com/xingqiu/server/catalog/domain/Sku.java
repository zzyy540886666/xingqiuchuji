package com.xingqiu.server.catalog.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("skus")
public class Sku {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private SkuType type;

    private Long brandId;

    private Long modelId;

    private String description;

    private String specsJson;

    private String subtitle;

    private Long originalPriceMinor;

    private String adaptedScenesText;

    private String stockStatusText;

    private String deliveryText;

    private SkuStatus status;

    private Integer stock;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum SkuType {
        RENT,
        BUY,
        SOFTWARE
    }

    public enum SkuStatus {
        ONLINE,
        OFFLINE
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public SkuType getType() { return type; }
    public void setType(SkuType type) { this.type = type; }

    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }

    public Long getModelId() { return modelId; }
    public void setModelId(Long modelId) { this.modelId = modelId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSpecsJson() { return specsJson; }
    public void setSpecsJson(String specsJson) { this.specsJson = specsJson; }

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

    public SkuStatus getStatus() { return status; }
    public void setStatus(SkuStatus status) { this.status = status; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
