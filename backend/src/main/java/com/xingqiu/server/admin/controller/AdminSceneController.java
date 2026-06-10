package com.xingqiu.server.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.appconfig.service.CatalogPlacementService;
import com.xingqiu.server.catalog.domain.Scene;
import com.xingqiu.server.catalog.domain.SceneSkuRel;
import com.xingqiu.server.catalog.mapper.SceneMapper;
import com.xingqiu.server.catalog.mapper.SceneSkuRelMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.CacheManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/scenes")
public class AdminSceneController {

    private final SceneMapper sceneMapper;
    private final SceneSkuRelMapper sceneSkuRelMapper;
    private final CatalogPlacementService catalogPlacementService;
    private final CacheManager cacheManager;

    public AdminSceneController(SceneMapper sceneMapper,
                                SceneSkuRelMapper sceneSkuRelMapper,
                                CatalogPlacementService catalogPlacementService,
                                CacheManager cacheManager) {
        this.sceneMapper = sceneMapper;
        this.sceneSkuRelMapper = sceneSkuRelMapper;
        this.catalogPlacementService = catalogPlacementService;
        this.cacheManager = cacheManager;
    }

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        Page<Scene> pageObj = new Page<>(page, pageSize);
        Page<Scene> result = sceneMapper.selectPage(pageObj,
                new LambdaQueryWrapper<Scene>().orderByAsc(Scene::getSortOrder).orderByAsc(Scene::getId));
        Map<Long, List<String>> tags = catalogPlacementService.getSceneTags();
        result.getRecords().forEach(scene -> {
            scene.setTags(tags.getOrDefault(scene.getId(), List.of()));
            scene.setSkuIds(findSkuIds(scene.getId()));
        });
        List<Map<String, Object>> items = result.getRecords().stream().map(this::toRow).toList();
        return ApiResponse.ok(PageResult.of(items, result.getTotal(), page, pageSize));
    }

    @PostMapping
    @Transactional
    public ApiResponse<Map<String, Object>> create(@RequestBody Scene scene) {
        validate(scene);
        if (scene.getSortOrder() == null) {
            scene.setSortOrder(0);
        }
        sceneMapper.insert(scene);
        catalogPlacementService.updateSceneTags(scene.getId(), normalizeTags(scene.getTags()));
        updateSkuRelations(scene.getId(), scene.getSkuIds());
        clearSceneCache();
        return ApiResponse.ok(toRow(scene));
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Scene scene) {
        Scene existing = sceneMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Scene not found");
        }
        validate(scene);
        scene.setId(id);
        if (scene.getSortOrder() == null) {
            scene.setSortOrder(existing.getSortOrder());
        }
        sceneMapper.updateById(scene);
        catalogPlacementService.updateSceneTags(id, normalizeTags(scene.getTags()));
        if (scene.getSkuIds() != null) {
            updateSkuRelations(id, scene.getSkuIds());
        }
        clearSceneCache();
        Scene updated = sceneMapper.selectById(id);
        updated.setTags(normalizeTags(scene.getTags()));
        updated.setSkuIds(findSkuIds(id));
        return ApiResponse.ok(toRow(updated));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sceneSkuRelMapper.delete(new LambdaQueryWrapper<SceneSkuRel>().eq(SceneSkuRel::getSceneId, id));
        sceneMapper.deleteById(id);
        catalogPlacementService.updateSceneTags(id, List.of());
        clearSceneCache();
        return ApiResponse.ok(null);
    }

    private Map<String, Object> toRow(Scene scene) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(scene.getId()));
        row.put("name", scene.getName());
        row.put("description", scene.getDescription());
        row.put("imageUrl", scene.getImageUrl());
        row.put("sortOrder", scene.getSortOrder());
        row.put("tags", scene.getTags() == null ? List.of() : scene.getTags());
        row.put("skuIds", (scene.getSkuIds() == null ? findSkuIds(scene.getId()) : scene.getSkuIds())
                .stream()
                .map(String::valueOf)
                .toList());
        row.put("status", "ACTIVE");
        row.put("skuCount", sceneSkuRelMapper.selectCount(
                new LambdaQueryWrapper<SceneSkuRel>().eq(SceneSkuRel::getSceneId, scene.getId())));
        return row;
    }

    private void validate(Scene scene) {
        if (scene.getName() == null || scene.getName().isBlank() || scene.getName().trim().length() > 50) {
            throw new BizException(ErrorCode.BAD_REQUEST, "场景名称不能为空且不能超过 50 个字符");
        }
        if (scene.getImageUrl() != null && scene.getImageUrl().length() > 512) {
            throw new BizException(ErrorCode.BAD_REQUEST, "场景图片地址不能超过 512 个字符");
        }
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags.stream()
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .limit(4)
                .toList();
    }

    private List<Long> findSkuIds(Long sceneId) {
        return sceneSkuRelMapper.selectList(new LambdaQueryWrapper<SceneSkuRel>()
                        .eq(SceneSkuRel::getSceneId, sceneId))
                .stream()
                .map(SceneSkuRel::getSkuId)
                .toList();
    }

    private void updateSkuRelations(Long sceneId, List<Long> skuIds) {
        sceneSkuRelMapper.delete(new LambdaQueryWrapper<SceneSkuRel>().eq(SceneSkuRel::getSceneId, sceneId));
        if (skuIds == null) {
            return;
        }
        skuIds.stream().distinct().forEach(skuId -> {
            SceneSkuRel relation = new SceneSkuRel();
            relation.setSceneId(sceneId);
            relation.setSkuId(skuId);
            sceneSkuRelMapper.insert(relation);
        });
    }

    private void clearSceneCache() {
        var cache = cacheManager.getCache("scenes");
        if (cache != null) {
            cache.clear();
        }
    }
}
