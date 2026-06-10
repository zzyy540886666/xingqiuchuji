package com.xingqiu.server.appconfig.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingqiu.server.appconfig.domain.AppConfig;
import com.xingqiu.server.appconfig.mapper.ConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CatalogPlacementService {

    private static final Logger log = LoggerFactory.getLogger(CatalogPlacementService.class);
    private static final String SCENE_TAGS_KEY = "scene_tags";
    private static final String RECOMMENDED_SKUS_KEY = "recommended_skus";
    private static final TypeReference<Map<String, List<String>>> SCENE_TAGS_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<Long>> SKU_IDS_TYPE = new TypeReference<>() {};

    private final ConfigMapper configMapper;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;

    public CatalogPlacementService(ConfigMapper configMapper, ObjectMapper objectMapper, CacheManager cacheManager) {
        this.configMapper = configMapper;
        this.objectMapper = objectMapper;
        this.cacheManager = cacheManager;
    }

    public Map<Long, List<String>> getSceneTags() {
        Map<String, List<String>> stored = readValue(SCENE_TAGS_KEY, SCENE_TAGS_TYPE, new LinkedHashMap<>());
        Map<Long, List<String>> result = new LinkedHashMap<>();
        stored.forEach((key, value) -> {
            try {
                result.put(Long.parseLong(key), value == null ? List.of() : List.copyOf(value));
            } catch (NumberFormatException ex) {
                log.warn("Ignoring invalid scene tag config key: {}", key);
            }
        });
        return result;
    }

    @Transactional
    public void updateSceneTags(Long sceneId, List<String> tags) {
        Map<String, List<String>> stored = readValue(SCENE_TAGS_KEY, SCENE_TAGS_TYPE, new LinkedHashMap<>());
        if (tags == null || tags.isEmpty()) {
            stored.remove(String.valueOf(sceneId));
        } else {
            stored.put(String.valueOf(sceneId), new ArrayList<>(tags));
        }
        writeValue(SCENE_TAGS_KEY, stored, "场景标签配置");
    }

    public Set<Long> getRecommendedSkuIds() {
        return new LinkedHashSet<>(readValue(RECOMMENDED_SKUS_KEY, SKU_IDS_TYPE, new ArrayList<>()));
    }

    @Transactional
    public boolean toggleRecommendedSku(Long skuId) {
        Set<Long> ids = getRecommendedSkuIds();
        boolean recommended;
        if (ids.remove(skuId)) {
            recommended = false;
        } else {
            ids.add(skuId);
            recommended = true;
        }
        writeValue(RECOMMENDED_SKUS_KEY, new ArrayList<>(ids), "首页推荐商品配置");
        return recommended;
    }

    private <T> T readValue(String key, TypeReference<T> type, T fallback) {
        AppConfig config = selectByKey(key);
        if (config == null || config.getValueJson() == null || config.getValueJson().isBlank()) {
            return fallback;
        }
        try {
            return objectMapper.readValue(config.getValueJson(), type);
        } catch (Exception ex) {
            log.warn("Failed to parse placement config {}: {}", key, ex.getMessage());
            return fallback;
        }
    }

    private void writeValue(String key, Object value, String description) {
        try {
            AppConfig config = selectByKey(key);
            if (config == null) {
                config = new AppConfig();
                config.setConfigKey(key);
                config.setVersion(1);
                config.setDescription(description);
            } else {
                config.setVersion((config.getVersion() == null ? 0 : config.getVersion()) + 1);
            }
            config.setValueJson(objectMapper.writeValueAsString(value));
            config.setEffectiveAt(LocalDateTime.now());
            if (config.getId() == null) {
                configMapper.insert(config);
            } else {
                configMapper.updateById(config);
            }
            var cache = cacheManager.getCache("configs");
            if (cache != null) {
                cache.clear();
            }
        } catch (Exception ex) {
            throw new IllegalStateException("写入运营位配置失败：" + key, ex);
        }
    }

    private AppConfig selectByKey(String key) {
        return configMapper.selectOne(new LambdaQueryWrapper<AppConfig>().eq(AppConfig::getConfigKey, key));
    }
}
