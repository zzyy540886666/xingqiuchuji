package com.xingqiu.server.catalog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.appconfig.service.CatalogPlacementService;
import com.xingqiu.server.catalog.domain.Scene;
import com.xingqiu.server.catalog.domain.SceneSkuRel;
import com.xingqiu.server.catalog.domain.Sku;
import com.xingqiu.server.catalog.dto.SkuDetailResponse;
import com.xingqiu.server.catalog.mapper.SceneMapper;
import com.xingqiu.server.catalog.mapper.SceneSkuRelMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SceneService {

    private static final Logger log = LoggerFactory.getLogger(SceneService.class);

    private final SceneMapper sceneMapper;
    private final SceneSkuRelMapper sceneSkuRelMapper;
    private final SkuService skuService;
    private final CatalogPlacementService catalogPlacementService;

    public SceneService(SceneMapper sceneMapper,
                        SceneSkuRelMapper sceneSkuRelMapper,
                        SkuService skuService,
                        CatalogPlacementService catalogPlacementService) {
        this.sceneMapper = sceneMapper;
        this.sceneSkuRelMapper = sceneSkuRelMapper;
        this.skuService = skuService;
        this.catalogPlacementService = catalogPlacementService;
    }

    @Cacheable(value = "scenes", key = "'all'")
    public List<Scene> listAll() {
        LambdaQueryWrapper<Scene> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Scene::getSortOrder);
        List<Scene> scenes = sceneMapper.selectList(wrapper);
        Map<Long, List<String>> tags = catalogPlacementService.getSceneTags();
        scenes.forEach(scene -> scene.setTags(tags.getOrDefault(scene.getId(), List.of())));
        return scenes;
    }

    @Cacheable(value = "scenes", key = "#sceneKey")
    public Map<String, Object> getSceneWithBundles(String sceneKey) {
        Scene scene = resolveScene(sceneKey);
        if (scene == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "场景不存在");
        }
        Long sceneId = scene.getId();

        LambdaQueryWrapper<SceneSkuRel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SceneSkuRel::getSceneId, sceneId);
        List<SceneSkuRel> rels = sceneSkuRelMapper.selectList(wrapper);

        List<SkuDetailResponse> skus = rels.stream()
                .map(rel -> {
                    try {
                        return skuService.getById(rel.getSkuId());
                    } catch (BizException e) {
                        log.debug("Skipping SKU {} for scene {}: {}", rel.getSkuId(), sceneId, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scene", scene);
        result.put("skus", skus);
        return result;
    }

    private Scene resolveScene(String sceneKey) {
        try {
            return sceneMapper.selectById(Long.parseLong(sceneKey));
        } catch (NumberFormatException e) {
            LambdaQueryWrapper<Scene> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Scene::getName, sceneKey);
            return sceneMapper.selectOne(wrapper);
        }
    }
}
