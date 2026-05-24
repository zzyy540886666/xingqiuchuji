package com.xingqiu.server.member.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("user_membership")
public class Membership {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    /** 会员等级: 0-3, 默认 0 */
    private Integer level;

    /** 是否原生用户（首次购买时标记） */
    private Boolean isNative;

    /** 终生消费额，单位：分 */
    private Long lifetimeSpendMinor;

    /** 星球卡到期时间 */
    private LocalDateTime planetCardExpiresAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Boolean getIsNative() { return isNative; }
    public void setIsNative(Boolean isNative) { this.isNative = isNative; }

    public Long getLifetimeSpendMinor() { return lifetimeSpendMinor; }
    public void setLifetimeSpendMinor(Long lifetimeSpendMinor) { this.lifetimeSpendMinor = lifetimeSpendMinor; }

    public LocalDateTime getPlanetCardExpiresAt() { return planetCardExpiresAt; }
    public void setPlanetCardExpiresAt(LocalDateTime planetCardExpiresAt) { this.planetCardExpiresAt = planetCardExpiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /** 创建默认会员记录 */
    public static Membership createDefault(Long userId) {
        Membership m = new Membership();
        m.setUserId(userId);
        m.setLevel(0);
        m.setIsNative(false);
        m.setLifetimeSpendMinor(0L);
        m.setCreatedAt(LocalDateTime.now());
        m.setUpdatedAt(LocalDateTime.now());
        return m;
    }
}
