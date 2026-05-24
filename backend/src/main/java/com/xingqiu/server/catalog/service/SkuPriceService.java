package com.xingqiu.server.catalog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.catalog.domain.SkuPrice;
import com.xingqiu.server.catalog.mapper.SkuPriceMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkuPriceService {

    private final SkuPriceMapper skuPriceMapper;

    public SkuPriceService(SkuPriceMapper skuPriceMapper) {
        this.skuPriceMapper = skuPriceMapper;
    }

    @Cacheable(value = "skus", key = "'price_' + #skuId")
    public List<SkuPrice> getBySkuId(Long skuId) {
        LambdaQueryWrapper<SkuPrice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuPrice::getSkuId, skuId);
        return skuPriceMapper.selectList(wrapper);
    }

    public List<Long> findSkuIds(LambdaQueryWrapper<SkuPrice> wrapper) {
        return skuPriceMapper.selectList(wrapper).stream()
                .map(SkuPrice::getSkuId)
                .distinct()
                .toList();
    }
}
