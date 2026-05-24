package com.xingqiu.server.catalog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.catalog.domain.CatalogFilterGroup;
import com.xingqiu.server.catalog.domain.CatalogFilterOption;
import com.xingqiu.server.catalog.domain.Scene;
import com.xingqiu.server.catalog.dto.SkuDetailResponse;
import com.xingqiu.server.catalog.dto.SkuListRequest;
import com.xingqiu.server.catalog.mapper.CatalogFilterGroupMapper;
import com.xingqiu.server.catalog.mapper.CatalogFilterOptionMapper;
import com.xingqiu.server.catalog.service.SceneService;
import com.xingqiu.server.catalog.service.SearchService;
import com.xingqiu.server.catalog.service.SkuService;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private final SkuService skuService;
    private final SceneService sceneService;
    private final SearchService searchService;
    private final CatalogFilterGroupMapper filterGroupMapper;
    private final CatalogFilterOptionMapper filterOptionMapper;

    public CatalogController(SkuService skuService,
                             SceneService sceneService,
                             SearchService searchService,
                             CatalogFilterGroupMapper filterGroupMapper,
                             CatalogFilterOptionMapper filterOptionMapper) {
        this.skuService = skuService;
        this.sceneService = sceneService;
        this.searchService = searchService;
        this.filterGroupMapper = filterGroupMapper;
        this.filterOptionMapper = filterOptionMapper;
    }

    @GetMapping("/skus")
    public ApiResponse<PageResult<SkuDetailResponse>> listSkus(SkuListRequest request) {
        PageResult<SkuDetailResponse> result = skuService.list(request);
        return ApiResponse.ok(result);
    }

    @GetMapping("/filters")
    public ApiResponse<List<Map<String, Object>>> listFilters() {
        List<CatalogFilterGroup> groups = filterGroupMapper.selectList(new LambdaQueryWrapper<CatalogFilterGroup>()
                .eq(CatalogFilterGroup::getEnabled, true)
                .orderByAsc(CatalogFilterGroup::getSortOrder));
        List<Map<String, Object>> rows = groups.stream().map(group -> {
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("id", group.getId());
            row.put("code", group.getCode());
            row.put("title", group.getTitle());
            row.put("filterField", group.getFilterField());
            List<CatalogFilterOption> options = filterOptionMapper.selectList(new LambdaQueryWrapper<CatalogFilterOption>()
                    .eq(CatalogFilterOption::getGroupId, group.getId())
                    .eq(CatalogFilterOption::getEnabled, true)
                    .orderByAsc(CatalogFilterOption::getSortOrder));
            row.put("options", options);
            return row;
        }).toList();
        return ApiResponse.ok(rows);
    }

    @GetMapping("/skus/{skuId}")
    public ApiResponse<SkuDetailResponse> getSku(@PathVariable Long skuId) {
        SkuDetailResponse detail = skuService.getById(skuId);
        return ApiResponse.ok(detail);
    }

    @GetMapping("/scenes")
    public ApiResponse<List<Scene>> listScenes() {
        List<Scene> scenes = sceneService.listAll();
        return ApiResponse.ok(scenes);
    }

    @GetMapping("/scenes/{sceneId}/bundles")
    public ApiResponse<Map<String, Object>> getSceneBundles(@PathVariable String sceneId) {
        Map<String, Object> bundles = sceneService.getSceneWithBundles(sceneId);
        return ApiResponse.ok(bundles);
    }

    @GetMapping("/search")
    public ApiResponse<PageResult<SkuDetailResponse>> search(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<SkuDetailResponse> result = searchService.search(keyword, page, pageSize);
        return ApiResponse.ok(result);
    }
}
