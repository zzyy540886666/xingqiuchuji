package com.xingqiu.server.catalog.service;

import com.xingqiu.server.catalog.dto.SkuDetailResponse;
import com.xingqiu.server.catalog.dto.SkuListRequest;
import com.xingqiu.server.common.response.PageResult;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final SkuService skuService;

    public SearchService(SkuService skuService) {
        this.skuService = skuService;
    }

    public PageResult<SkuDetailResponse> search(String keyword, int page, int pageSize) {
        SkuListRequest request = new SkuListRequest();
        request.setQ(keyword);
        request.setPage(page);
        request.setPageSize(pageSize);
        return skuService.list(request);
    }
}
