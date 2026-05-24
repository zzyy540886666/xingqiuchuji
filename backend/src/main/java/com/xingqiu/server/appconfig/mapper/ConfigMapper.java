package com.xingqiu.server.appconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingqiu.server.appconfig.domain.AppConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConfigMapper extends BaseMapper<AppConfig> {
}
