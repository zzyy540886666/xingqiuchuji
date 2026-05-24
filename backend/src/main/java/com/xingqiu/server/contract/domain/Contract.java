package com.xingqiu.server.contract.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("contracts")
public class Contract {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;

    private String templateVersion;

    private String cosKey;

    private String pdfHash;

    private SignStatus signStatus;

    private LocalDateTime createdAt;

    public enum SignStatus {
        PENDING,
        SIGNED
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getTemplateVersion() { return templateVersion; }
    public void setTemplateVersion(String templateVersion) { this.templateVersion = templateVersion; }

    public String getCosKey() { return cosKey; }
    public void setCosKey(String cosKey) { this.cosKey = cosKey; }

    public String getPdfHash() { return pdfHash; }
    public void setPdfHash(String pdfHash) { this.pdfHash = pdfHash; }

    public SignStatus getSignStatus() { return signStatus; }
    public void setSignStatus(SignStatus signStatus) { this.signStatus = signStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
