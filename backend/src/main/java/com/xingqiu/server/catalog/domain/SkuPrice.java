package com.xingqiu.server.catalog.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sku_prices")
public class SkuPrice {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long skuId;

    private String priceType;

    /** Price in cents (integer). Never use float/double for money. */
    private Long priceMinor;

    private Integer minDuration;

    private Integer maxDuration;

    /** Daily rate in cents (integer). Never use float/double for money. */
    private Long dailyRateMinor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }

    public String getPriceType() { return priceType; }
    public void setPriceType(String priceType) { this.priceType = priceType; }

    public Long getPriceMinor() { return priceMinor; }
    public void setPriceMinor(Long priceMinor) { this.priceMinor = priceMinor; }

    public Integer getMinDuration() { return minDuration; }
    public void setMinDuration(Integer minDuration) { this.minDuration = minDuration; }

    public Integer getMaxDuration() { return maxDuration; }
    public void setMaxDuration(Integer maxDuration) { this.maxDuration = maxDuration; }

    public Long getDailyRateMinor() { return dailyRateMinor; }
    public void setDailyRateMinor(Long dailyRateMinor) { this.dailyRateMinor = dailyRateMinor; }
}
