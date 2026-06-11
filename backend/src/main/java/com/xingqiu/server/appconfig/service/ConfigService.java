package com.xingqiu.server.appconfig.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingqiu.server.appconfig.domain.AppConfig;
import com.xingqiu.server.appconfig.mapper.ConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConfigService {

    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);

    private final ConfigMapper configMapper;
    private final ObjectMapper objectMapper;

    /**
     * Fallback config version used when no config rows exist in the database.
     * Overridable via xingqiu.config.version in application.yml to allow
     * version bumps without recompilation.
     */
    @Value("${xingqiu.config.version:1}")
    private int fallbackConfigVersion;

    public ConfigService(ConfigMapper configMapper, ObjectMapper objectMapper) {
        this.configMapper = configMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Get the current active config version derived from the database.
     * Uses the maximum version across all app_configs rows, which increments
     * each time any config key is updated via AdminConfigService.
     * Falls back to the configured {@code xingqiu.config.version} property
     * (default 1) when the table is empty.
     */
    public int getCurrentConfigVersion() {
        return configMapper.selectList(new LambdaQueryWrapper<>())
                .stream()
                .mapToInt(c -> c.getVersion() == null ? 0 : c.getVersion())
                .max()
                .orElse(fallbackConfigVersion);
    }

    /**
     * 获取应用配置。
     * 如果客户端版本低于服务端版本，返回完整配置 JSON；
     * 如果版本一致，返回空 data，表示无需更新。
     *
     * @param clientVersion 客户端本地配置版本号
     * @return 配置 Map
     */
    @Cacheable(value = "configs", key = "'appConfig_' + (#clientVersion != null ? #clientVersion : 'null')", unless = "#result == null")
    public Map<String, Object> getAppConfig(Integer clientVersion) {
        Map<String, Object> result = new LinkedHashMap<>();
        int serverVersion = getCurrentConfigVersion();
        result.put("version", serverVersion);

        if (clientVersion != null && clientVersion >= serverVersion) {
            result.put("upToDate", true);
            return result;
        }

        result.put("upToDate", false);

        // 从数据库加载所有配置行
        List<AppConfig> configs = configMapper.selectList(new LambdaQueryWrapper<>());

        // 构建配置内容
        result.put("membershipRules", loadConfigSection(configs, "membership_rules", defaultMembershipRules()));
        result.put("planetCards", loadConfigSection(configs, "planet_cards", defaultPlanetCards()));
        result.put("topics", loadConfigSection(configs, "topics", defaultTopics()));
        result.put("qaBlocks", loadConfigSection(configs, "qa_blocks", defaultQaBlocks()));
        result.put("trusteeshipPricingNote", loadConfigValue(configs, "trusteeship_pricing_note",
                "托管收益按日结算，具体费率以合同为准"));
        result.put("trusteeshipDailyRateMinor", loadConfigLong(configs, "trusteeship_daily_rate_minor", 300L));
        result.put("banners", loadConfigSection(configs, "banners", new ArrayList<>()));
        result.put("hotKeywords", loadConfigSection(configs, "hot_keywords", new ArrayList<>()));
        result.put("sceneTags", loadConfigSection(configs, "scene_tags", new LinkedHashMap<>()));

        return result;
    }

    /**
     * 获取会员等级阈值规则。
     * 优先从数据库加载，无覆盖时返回内置默认值。
     * 供服务端线程调用，独立于客户端版本缓存。
     */
    @Cacheable(value = "configs", key = "'membership_rules'")
    public List<Map<String, Object>> getMembershipRules() {
        List<AppConfig> configs = configMapper.selectList(new LambdaQueryWrapper<>());
        Object section = loadConfigSection(configs, "membership_rules", defaultMembershipRules());
        if (section instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) section;
            return list;
        }
        return defaultMembershipRules();
    }

    private Object loadConfigSection(List<AppConfig> configs, String key, Object defaultVal) {
        return configs.stream()
                .filter(c -> key.equals(c.getConfigKey()))
                .findFirst()
                .map(c -> parseJsonSafe(c.getValueJson(), defaultVal))
                .orElse(defaultVal);
    }

    private String loadConfigValue(List<AppConfig> configs, String key, String defaultVal) {
        return configs.stream()
                .filter(c -> key.equals(c.getConfigKey()))
                .findFirst()
                .map(AppConfig::getValueJson)
                .orElse(defaultVal);
    }

    private Long loadConfigLong(List<AppConfig> configs, String key, Long defaultVal) {
        String raw = configs.stream()
                .filter(c -> key.equals(c.getConfigKey()))
                .findFirst()
                .map(AppConfig::getValueJson)
                .orElse(null);
        if (raw == null || raw.isBlank()) {
            return defaultVal;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse config long for key={}, using default: {}", key, e.getMessage());
            return defaultVal;
        }
    }

    /**
     * 获取平台托管日租金（单位：分），供 TrusteeshipService 使用。
     */
    public Long getTrusteeshipDailyRateMinor() {
        List<AppConfig> configs = configMapper.selectList(new LambdaQueryWrapper<>());
        return loadConfigLong(configs, "trusteeship_daily_rate_minor", 300L);
    }

    private Object parseJsonSafe(String json, Object fallback) {
        if (json == null || json.isBlank()) {
            return fallback;
        }
        try {
            if (json.trim().startsWith("[")) {
                return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
            }
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse config json, using fallback: {}", e.getMessage());
            return fallback;
        }
    }

    // ---- built-in defaults (used when DB has no override) ----

    private List<Map<String, Object>> defaultMembershipRules() {
        List<Map<String, Object>> rules = new ArrayList<>();

        Map<String, Object> l0 = new LinkedHashMap<>();
        l0.put("level", 0);
        l0.put("name", "Lv.0 星际访客");
        l0.put("thresholdDesc", "注册即享，可浏览下单");
        l0.put("thresholdMinor", 0L);
        rules.add(l0);

        Map<String, Object> l1 = new LinkedHashMap<>();
        l1.put("level", 1);
        l1.put("name", "Lv.1 星球探索者");
        l1.put("thresholdDesc", "首单或付费 99 元/年");
        l1.put("thresholdMinor", 9_900L);
        rules.add(l1);

        Map<String, Object> l2 = new LinkedHashMap<>();
        l2.put("level", 2);
        l2.put("name", "Lv.2 星球开拓者");
        l2.put("thresholdDesc", "累计消费满 5000 元或 299 元/年");
        l2.put("thresholdMinor", 500_000L);
        rules.add(l2);

        Map<String, Object> l3 = new LinkedHashMap<>();
        l3.put("level", 3);
        l3.put("name", "Lv.3 星球领主");
        l3.put("thresholdDesc", "累计消费满 20000 元或 999 元/年");
        l3.put("thresholdMinor", 2_000_000L);
        rules.add(l3);

        return rules;
    }

    private List<Map<String, Object>> defaultPlanetCards() {
        return new ArrayList<>();
    }

    private List<Map<String, Object>> defaultTopics() {
        return new ArrayList<>();
    }

    private Map<String, Object> defaultQaBlocks() {
        return new LinkedHashMap<>();
    }
}
