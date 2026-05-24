package com.xingqiu.server.appconfig.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("app_configs")
public class AppConfig {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 配置键，唯一 */
    private String configKey;

    /** 配置值，JSON 文本 */
    private String valueJson;

    /** 版本号，递增 */
    private Integer version;

    /** 生效时间 */
    private LocalDateTime effectiveAt;

    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }

    public String getValueJson() { return valueJson; }
    public void setValueJson(String valueJson) { this.valueJson = valueJson; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public LocalDateTime getEffectiveAt() { return effectiveAt; }
    public void setEffectiveAt(LocalDateTime effectiveAt) { this.effectiveAt = effectiveAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
