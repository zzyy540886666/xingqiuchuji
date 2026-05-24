package com.xingqiu.server.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingqiu.server.wallet.domain.Wallet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletMapper extends BaseMapper<Wallet> {
}
