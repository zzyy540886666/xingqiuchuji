package com.xingqiu.server.wallet.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("wallets")
public class Wallet {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户 ID，唯一 */
    private Long userId;

    /** 可用余额，单位：分 */
    private Long balanceMinor;

    /** 冻结金额，单位：分 */
    private Long frozenMinor;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getBalanceMinor() { return balanceMinor; }
    public void setBalanceMinor(Long balanceMinor) { this.balanceMinor = balanceMinor; }

    public Long getFrozenMinor() { return frozenMinor; }
    public void setFrozenMinor(Long frozenMinor) { this.frozenMinor = frozenMinor; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Wallet createDefault(Long userId) {
        Wallet w = new Wallet();
        w.setUserId(userId);
        w.setBalanceMinor(0L);
        w.setFrozenMinor(0L);
        w.setCreatedAt(LocalDateTime.now());
        w.setUpdatedAt(LocalDateTime.now());
        return w;
    }
}
