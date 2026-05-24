package com.xingqiu.server.catalog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.catalog.domain.SkuMedia;
import com.xingqiu.server.catalog.mapper.SkuMediaMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkuMediaService {

    private final SkuMediaMapper skuMediaMapper;

    public SkuMediaService(SkuMediaMapper skuMediaMapper) {
        this.skuMediaMapper = skuMediaMapper;
    }

    @Cacheable(value = "skus", key = "'media_' + #skuId")
    public List<SkuMedia> getBySkuId(Long skuId) {
        LambdaQueryWrapper<SkuMedia> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuMedia::getSkuId, skuId);
        wrapper.orderByAsc(SkuMedia::getSortOrder);
        return skuMediaMapper.selectList(wrapper);
    }
}
