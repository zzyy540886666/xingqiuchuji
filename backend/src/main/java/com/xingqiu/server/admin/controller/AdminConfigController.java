package com.xingqiu.server.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingqiu.server.admin.service.AdminConfigService;
import com.xingqiu.server.appconfig.domain.AppConfig;
import com.xingqiu.server.appconfig.mapper.ConfigMapper;
import com.xingqiu.server.common.response.ApiResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/configs")
public class AdminConfigController {

    private static final TypeReference<List<Map<String, Object>>> LIST_OF_MAPS = new TypeReference<>() {};

    private final AdminConfigService adminConfigService;
    private final ConfigMapper configMapper;
    private final ObjectMapper objectMapper;

    public AdminConfigController(AdminConfigService adminConfigService,
                                 ConfigMapper configMapper,
                                 ObjectMapper objectMapper) {
        this.adminConfigService = adminConfigService;
        this.configMapper = configMapper;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ApiResponse<List<AppConfig>> list() {
        return ApiResponse.ok(adminConfigService.listConfigs());
    }

    @PostMapping
    public ApiResponse<AppConfig> createRaw(@RequestBody AppConfig config) {
        if (config.getVersion() == null) {
            config.setVersion(1);
        }
        config.setEffectiveAt(LocalDateTime.now());
        configMapper.insert(config);
        return ApiResponse.ok(config);
    }

    @PutMapping("/{id}")
    public ApiResponse<AppConfig> updateRaw(@PathVariable Long id, @RequestBody AppConfig body) {
        AppConfig config = configMapper.selectById(id);
        if (config == null) {
            config = new AppConfig();
            config.setId(id);
            config.setConfigKey(body.getConfigKey() == null ? "config." + id : body.getConfigKey());
            config.setVersion(1);
        }
        if (body.getConfigKey() != null) config.setConfigKey(body.getConfigKey());
        if (body.getValueJson() != null) config.setValueJson(body.getValueJson());
        if (body.getDescription() != null) config.setDescription(body.getDescription());
        config.setVersion((config.getVersion() == null ? 0 : config.getVersion()) + 1);
        config.setEffectiveAt(LocalDateTime.now());
        saveConfig(config);
        return ApiResponse.ok(config);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRaw(@PathVariable Long id) {
        configMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/banners")
    public ApiResponse<List<Map<String, Object>>> listBanners() {
        return ApiResponse.ok(readList("banners", defaultBanners()));
    }

    @PostMapping("/banners")
    @Transactional
    public ApiResponse<Map<String, Object>> createBanner(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(upsertItem("banners", defaultBanners(), null, normalizeBanner(body)));
    }

    @PutMapping("/banners/{id}")
    @Transactional
    public ApiResponse<Map<String, Object>> updateBanner(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(upsertItem("banners", defaultBanners(), id, normalizeBanner(body)));
    }

    @DeleteMapping("/banners/{id}")
    @Transactional
    public ApiResponse<Void> deleteBanner(@PathVariable Long id) {
        deleteItem("banners", defaultBanners(), id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/topics")
    public ApiResponse<List<Map<String, Object>>> listTopics() {
        return ApiResponse.ok(readList("admin.topics", defaultTopics()));
    }

    @PostMapping("/topics")
    @Transactional
    public ApiResponse<Map<String, Object>> createTopic(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(upsertItem("admin.topics", defaultTopics(), null, normalizeTopic(body)));
    }

    @PutMapping("/topics/{id}")
    @Transactional
    public ApiResponse<Map<String, Object>> updateTopic(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(upsertItem("admin.topics", defaultTopics(), id, normalizeTopic(body)));
    }

    @PostMapping("/topics/{id}/publish")
    @Transactional
    public ApiResponse<Void> publishTopic(@PathVariable Long id) {
        changeItemStatus("admin.topics", defaultTopics(), id, "PUBLISHED");
        return ApiResponse.ok(null);
    }

    @PostMapping("/topics/{id}/offline")
    @Transactional
    public ApiResponse<Void> offlineTopic(@PathVariable Long id) {
        changeItemStatus("admin.topics", defaultTopics(), id, "OFFLINE");
        return ApiResponse.ok(null);
    }

    @GetMapping("/versions")
    public ApiResponse<List<Map<String, Object>>> listVersions() {
        return ApiResponse.ok(readList("admin.versions", defaultVersions()));
    }

    @PostMapping("/rollback")
    public ApiResponse<Void> rollback(@RequestBody Map<String, Object> body) {
        List<Map<String, Object>> versions = readList("admin.versions", defaultVersions());
        for (Map<String, Object> version : versions) {
            version.put("status", "HISTORY");
        }
        Map<String, Object> current = new LinkedHashMap<>();
        current.put("version", versions.stream().mapToInt(v -> ((Number) v.get("version")).intValue()).max().orElse(1) + 1);
        current.put("publishedBy", "管理员");
        current.put("publishedAt", LocalDateTime.now().toString());
        current.put("description", "回滚到版本 " + body.get("targetVersion"));
        current.put("status", "ACTIVE");
        versions.add(0, current);
        writeList("admin.versions", versions, "Config publish history");
        return ApiResponse.ok(null);
    }

    @PostMapping("/refresh")
    public ApiResponse<Void> refreshCache() {
        adminConfigService.refreshCache();
        return ApiResponse.ok(null);
    }

    private Map<String, Object> normalizeBanner(Map<String, Object> body) {
        Map<String, Object> item = new LinkedHashMap<>(body);
        item.putIfAbsent("position", "HOME_RENT_CARD");
        item.putIfAbsent("type", "IMAGE");
        item.putIfAbsent("status", "ACTIVE");
        item.putIfAbsent("sortOrder", 0);
        return item;
    }

    private Map<String, Object> normalizeTopic(Map<String, Object> body) {
        Map<String, Object> item = new LinkedHashMap<>(body);
        item.putIfAbsent("skuCount", 0);
        item.putIfAbsent("status", "DRAFT");
        item.putIfAbsent("effectiveAt", LocalDateTime.now().toString());
        return item;
    }

    private Map<String, Object> upsertItem(String key, List<Map<String, Object>> defaults,
                                           Long id, Map<String, Object> item) {
        List<Map<String, Object>> items = readList(key, defaults);
        Long itemId = id == null ? number(item.get("id"), null) : id;
        if (itemId == null) {
            itemId = items.stream().mapToLong(i -> number(i.get("id"), 0L)).max().orElse(0L) + 1;
        }
        item.put("id", itemId);
        boolean replaced = false;
        for (int i = 0; i < items.size(); i++) {
            if (number(items.get(i).get("id"), -1L).equals(itemId)) {
                items.set(i, item);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            items.add(item);
        }
        writeList(key, items, key);
        return item;
    }

    private void deleteItem(String key, List<Map<String, Object>> defaults, Long id) {
        List<Map<String, Object>> items = readList(key, defaults);
        items.removeIf(item -> number(item.get("id"), -1L).equals(id));
        writeList(key, items, key);
    }

    private void changeItemStatus(String key, List<Map<String, Object>> defaults, Long id, String status) {
        List<Map<String, Object>> items = readList(key, defaults);
        for (Map<String, Object> item : items) {
            if (number(item.get("id"), -1L).equals(id)) {
                item.put("status", status);
            }
        }
        writeList(key, items, key);
    }

    private List<Map<String, Object>> readList(String key, List<Map<String, Object>> defaults) {
        AppConfig config = selectByKey(key);
        if (config == null || config.getValueJson() == null || config.getValueJson().isBlank()) {
            writeList(key, defaults, key);
            return new ArrayList<>(defaults);
        }
        try {
            List<Map<String, Object>> items = objectMapper.readValue(config.getValueJson(), LIST_OF_MAPS);
            localizeLegacyText(key, items);
            writeList(key, items, key);
            return items;
        } catch (Exception ex) {
            return new ArrayList<>(defaults);
        }
    }

    private void localizeLegacyText(String key, List<Map<String, Object>> items) {
        if ("banners".equals(key)) {
            for (Map<String, Object> item : items) {
                String title = String.valueOf(item.getOrDefault("title", ""));
                if ("Spring rental campaign".equals(title)) item.put("title", "春季租赁活动");
                if ("G1 humanoid launch".equals(title)) item.put("title", "人形机器人新品上线");
                if ("Industrial inspection bundle".equals(title)) item.put("title", "工业巡检组合");
            }
        }
        if ("admin.topics".equals(key)) {
            for (Map<String, Object> item : items) {
                String title = String.valueOf(item.getOrDefault("title", ""));
                if ("Education lab robot package".equals(title)) {
                    item.put("title", "科研教育实验室组合");
                    item.put("content", "面向教学和科研实验室的人形与四足机器人组合。");
                }
                if ("Factory inspection starter kit".equals(title)) {
                    item.put("title", "工厂巡检入门组合");
                    item.put("content", "覆盖巡检路线、设备租赁和维护流程的组合方案。");
                }
                if ("Event performance robot show".equals(title)) {
                    item.put("title", "展会表演机器人组合");
                    item.put("content", "适合展会和表演场景的短租组合方案。");
                }
            }
        }
        if ("admin.versions".equals(key)) {
            for (Map<String, Object> item : items) {
                String publisher = String.valueOf(item.getOrDefault("publishedBy", ""));
                if ("admin".equals(publisher)) item.put("publishedBy", "管理员");
                if ("operator".equals(publisher)) item.put("publishedBy", "运营人员");
                String description = String.valueOf(item.getOrDefault("description", ""));
                if ("Updated banners, topics, and search keywords.".equals(description)) item.put("description", "更新运营位、专题和搜索词。");
                if ("Added inspection campaign assets.".equals(description)) item.put("description", "新增巡检活动素材。");
                if ("Initial admin operation configuration.".equals(description)) item.put("description", "初始化后台运营配置。");
            }
        }
    }

    private void writeList(String key, List<Map<String, Object>> items, String description) {
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
            config.setValueJson(objectMapper.writeValueAsString(items));
            config.setEffectiveAt(LocalDateTime.now());
            saveConfig(config);
            adminConfigService.refreshCache();
        } catch (Exception ex) {
            throw new IllegalStateException("写入配置失败：" + key, ex);
        }
    }

    private AppConfig selectByKey(String key) {
        return configMapper.selectOne(new LambdaQueryWrapper<AppConfig>().eq(AppConfig::getConfigKey, key));
    }

    private void saveConfig(AppConfig config) {
        if (config.getId() != null && configMapper.selectById(config.getId()) != null) {
            configMapper.updateById(config);
        } else {
            configMapper.insert(config);
        }
    }

    private List<Map<String, Object>> defaultBanners() {
        return new ArrayList<>();
    }

    private List<Map<String, Object>> defaultTopics() {
        return new ArrayList<>();
    }

    private List<Map<String, Object>> defaultVersions() {
        return new ArrayList<>(List.of(
                map("version", 3, "publishedBy", "管理员", "publishedAt", "2026-05-24T10:00:00", "description", "更新运营位、专题和搜索词。", "status", "ACTIVE"),
                map("version", 2, "publishedBy", "运营人员", "publishedAt", "2026-05-22T16:30:00", "description", "新增巡检活动素材。", "status", "HISTORY"),
                map("version", 1, "publishedBy", "管理员", "publishedAt", "2026-05-20T09:00:00", "description", "初始化后台运营配置。", "status", "HISTORY")
        ));
    }

    private Map<String, Object> map(Object... kv) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            result.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return result;
    }

    private Long number(Object value, Long fallback) {
        if (value == null || String.valueOf(value).isBlank()) return fallback;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(String.valueOf(value));
    }
}
