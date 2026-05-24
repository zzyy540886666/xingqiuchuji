package com.xingqiu.server.member.dto;

import com.xingqiu.server.member.domain.MembershipBenefitGrant;

import java.time.LocalDateTime;
import java.util.List;

public class MembershipResponse {

    private Integer level;
    private Boolean isNative;
    private Long lifetimeSpendMinor;
    private LocalDateTime planetCardExpiresAt;
    private List<MembershipBenefitGrant> benefits;

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Boolean getIsNative() { return isNative; }
    public void setIsNative(Boolean isNative) { this.isNative = isNative; }

    public Long getLifetimeSpendMinor() { return lifetimeSpendMinor; }
    public void setLifetimeSpendMinor(Long lifetimeSpendMinor) { this.lifetimeSpendMinor = lifetimeSpendMinor; }

    public LocalDateTime getPlanetCardExpiresAt() { return planetCardExpiresAt; }
    public void setPlanetCardExpiresAt(LocalDateTime planetCardExpiresAt) { this.planetCardExpiresAt = planetCardExpiresAt; }

    public List<MembershipBenefitGrant> getBenefits() { return benefits; }
    public void setBenefits(List<MembershipBenefitGrant> benefits) { this.benefits = benefits; }
}
