package com.xingqiu.server.job.service;

import com.xingqiu.server.auth.domain.User;
import com.xingqiu.server.auth.mapper.UserMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.job.adapter.ImClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ImTokenService {

    private static final Logger log = LoggerFactory.getLogger(ImTokenService.class);

    private final ImClient imClient;
    private final UserMapper userMapper;

    public ImTokenService(ImClient imClient, UserMapper userMapper) {
        this.imClient = imClient;
        this.userMapper = userMapper;
    }

    /**
     * Get IM token for the current user. Returns token, appKey, userId.
     */
    public Map<String, Object> getImToken(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        String nickname = user.getNickname() != null ? user.getNickname() : "用户" + userId;
        String avatarUrl = user.getAvatarUrl() != null ? user.getAvatarUrl() : "";

        Map<String, Object> result = imClient.generateToken(userId, nickname, avatarUrl);

        log.info("IM token generated for userId={}", userId);

        return result;
    }
}
