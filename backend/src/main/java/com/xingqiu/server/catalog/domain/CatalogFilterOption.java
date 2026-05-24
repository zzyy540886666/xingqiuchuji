package com.xingqiu.server.catalog.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("catalog_filter_options")
public class CatalogFilterOption {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long groupId;

    private String label;

    private String value;

    private Long minPriceMinor;

    private Long maxPriceMinor;

    private Integer sortOrder;

    private Boolean enabled;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public Long getMinPriceMinor() { return minPriceMinor; }
    public void setMinPriceMinor(Long minPriceMinor) { this.minPriceMinor = minPriceMinor; }

    public Long getMaxPriceMinor() { return maxPriceMinor; }
    public void setMaxPriceMinor(Long maxPriceMinor) { this.maxPriceMinor = maxPriceMinor; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
