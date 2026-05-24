package com.xingqiu.server.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingqiu.server.member.domain.Membership;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MembershipMapper extends BaseMapper<Membership> {
}
