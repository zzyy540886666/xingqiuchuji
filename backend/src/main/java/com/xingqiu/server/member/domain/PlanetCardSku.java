package com.xingqiu.server.member.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("planet_card_skus")
public class PlanetCardSku {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    /** 有效期天数 */
    private Integer durationDays;

    /** 价格，单位：分 */
    private Long priceMinor;

    /** 购买后获得的权益等级 */
    private Integer benefitLevel;

    /** 库存 */
    private Integer stock;

    /** 状态: ON_SALE / SOLD_OUT / OFF */
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public Long getPriceMinor() { return priceMinor; }
    public void setPriceMinor(Long priceMinor) { this.priceMinor = priceMinor; }

    public Integer getBenefitLevel() { return benefitLevel; }
    public void setBenefitLevel(Integer benefitLevel) { this.benefitLevel = benefitLevel; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
