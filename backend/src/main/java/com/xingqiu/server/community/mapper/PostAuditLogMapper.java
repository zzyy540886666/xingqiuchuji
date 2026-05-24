package com.xingqiu.server.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingqiu.server.community.domain.PostAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostAuditLogMapper extends BaseMapper<PostAuditLog> {
}
