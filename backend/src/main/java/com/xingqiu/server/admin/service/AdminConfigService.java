package com.xingqiu.server.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.appconfig.domain.AppConfig;
import com.xingqiu.server.appconfig.mapper.ConfigMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminConfigService {

    private static final Logger log = LoggerFactory.getLogger(AdminConfigService.class);

    private final ConfigMapper configMapper;
    private final CacheManager cacheManager;

    public AdminConfigService(ConfigMapper configMapper, CacheManager cacheManager) {
        this.configMapper = configMapper;
        this.cacheManager = cacheManager;
    }

    public List<AppConfig> listConfigs() {
        LambdaQueryWrapper<AppConfig> query = new LambdaQueryWrapper<>();
        query.orderByAsc(AppConfig::getConfigKey);
        return configMapper.selectList(query);
    }

    public void updateConfig(String key, String valueJson) {
        LambdaQueryWrapper<AppConfig> query = new LambdaQueryWrapper<>();
        query.eq(AppConfig::getConfigKey, key);
        AppConfig config = configMapper.selectOne(query);

        if (config == null) {
            config = new AppConfig();
            config.setConfigKey(key);
            config.setValueJson(valueJson);
            config.setVersion(1);
            config.setEffectiveAt(LocalDateTime.now());
            configMapper.insert(config);
        } else {
            config.setValueJson(valueJson);
            config.setVersion(config.getVersion() + 1);
            config.setEffectiveAt(LocalDateTime.now());
            configMapper.updateById(config);
        }

        // Invalidate cache
        var cache = cacheManager.getCache("configs");
        if (cache != null) {
            cache.clear();
        }
        log.info("Config updated: key={}, newVersion={}", key, config.getVersion());
    }

    public void refreshCache() {
        var cache = cacheManager.getCache("configs");
        if (cache != null) {
            cache.clear();
        }
        log.info("Config cache refreshed by admin");
    }
}
