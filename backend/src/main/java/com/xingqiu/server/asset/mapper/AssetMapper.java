package com.xingqiu.server.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingqiu.server.asset.domain.UserAsset;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AssetMapper extends BaseMapper<UserAsset> {
}
