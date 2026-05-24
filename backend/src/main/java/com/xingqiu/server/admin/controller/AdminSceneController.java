package com.xingqiu.server.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/scenes")
public class AdminSceneController {

    private final SceneMapper sceneMapper;
    private final SceneSkuRelMapper sceneSkuRelMapper;

    public AdminSceneController(SceneMapper sceneMapper, SceneSkuRelMapper sceneSkuRelMapper) {
        this.sceneMapper = sceneMapper;
        this.sceneSkuRelMapper = sceneSkuRelMapper;
    }

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        Page<Scene> pageObj = new Page<>(page, pageSize);
        Page<Scene> result = sceneMapper.selectPage(pageObj,
                new LambdaQueryWrapper<Scene>().orderByAsc(Scene::getSortOrder).orderByAsc(Scene::getId));
        List<Map<String, Object>> items = result.getRecords().stream().map(this::toRow).toList();
        return ApiResponse.ok(PageResult.of(items, result.getTotal(), page, pageSize));
    }

    @PostMapping
    @Transactional
    public ApiResponse<Map<String, Object>> create(@RequestBody Scene scene) {
        if (scene.getSortOrder() == null) {
            scene.setSortOrder(0);
        }
        sceneMapper.insert(scene);
        return ApiResponse.ok(toRow(scene));
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Scene scene) {
        Scene existing = sceneMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Scene not found");
        }
        scene.setId(id);
        if (scene.getSortOrder() == null) {
            scene.setSortOrder(existing.getSortOrder());
        }
        sceneMapper.updateById(scene);
        return ApiResponse.ok(toRow(sceneMapper.selectById(id)));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sceneSkuRelMapper.delete(new LambdaQueryWrapper<SceneSkuRel>().eq(SceneSkuRel::getSceneId, id));
        sceneMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    private Map<String, Object> toRow(Scene scene) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", scene.getId());
        row.put("name", scene.getName());
        row.put("description", scene.getDescription());
        row.put("imageUrl", scene.getImageUrl());
        row.put("sortOrder", scene.getSortOrder());
        row.put("status", "ACTIVE");
        row.put("skuCount", sceneSkuRelMapper.selectCount(
                new LambdaQueryWrapper<SceneSkuRel>().eq(SceneSkuRel::getSceneId, scene.getId())));
        return row;
    }
}
