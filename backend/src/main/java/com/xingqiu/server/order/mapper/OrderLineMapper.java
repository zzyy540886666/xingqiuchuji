package com.xingqiu.server.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingqiu.server.order.domain.OrderLine;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderLineMapper extends BaseMapper<OrderLine> {
}
