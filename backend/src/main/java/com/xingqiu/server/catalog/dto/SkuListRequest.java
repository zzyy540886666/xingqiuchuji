package com.xingqiu.server.catalog.dto;

public class SkuListRequest {

    private String type;

    private Long brandId;

    private Long minPrice;

    private Long maxPrice;

    private Long modelId;

    private String q;

    private Integer page;

    private Integer pageSize;

    public SkuListRequest() {
        this.page = 1;
        this.pageSize = 20;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }

    public Long getMinPrice() { return minPrice; }
    public void setMinPrice(Long minPrice) { this.minPrice = minPrice; }

    public Long getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Long maxPrice) { this.maxPrice = maxPrice; }

    public Long getModelId() { return modelId; }
    public void setModelId(Long modelId) { this.modelId = modelId; }

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
