package com.xingqiu.server.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.auth.adapter.WeChatMiniProgramClient;
import com.xingqiu.server.auth.domain.User;
import com.xingqiu.server.auth.dto.LoginRequest;
import com.xingqiu.server.auth.dto.LoginResponse;
import com.xingqiu.server.auth.dto.UserSummary;
import com.xingqiu.server.auth.mapper.UserMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class WeChatLoginService {

    private static final Logger log = LoggerFactory.getLogger(WeChatLoginService.class);

    private final UserMapper userMapper;
    private final WeChatMiniProgramClient weChatClient;
    private final JwtUtil jwtUtil;
    private final long jwtExpirationSeconds;

    public WeChatLoginService(UserMapper userMapper,
                              WeChatMiniProgramClient weChatClient,
                              JwtUtil jwtUtil,
                              @Value("${xingqiu.jwt.expiration-seconds}") long jwtExpirationSeconds) {
        this.userMapper = userMapper;
        this.weChatClient = weChatClient;
        this.jwtUtil = jwtUtil;
        this.jwtExpirationSeconds = jwtExpirationSeconds;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. Call WeChat code2session
        Map<String, Object> sessionData = weChatClient.code2session(request.getCode());

        Object errcode = sessionData.get("errcode");
        if (errcode != null && ((Number) errcode).intValue() != 0) {
            String errmsg = (String) sessionData.getOrDefault("errmsg", "unknown error");
            log.warn("WeChat code2session failed: {} - {}", errcode, errmsg);
            throw new BizException(ErrorCode.AUTH_INVALID_CODE, "微信授权失败: " + errmsg);
        }

        String openid = (String) sessionData.get("openid");
        if (openid == null || openid.isBlank()) {
            throw new BizException(ErrorCode.AUTH_INVALID_CODE, "微信授权失败，未获取到openid");
        }

        String unionid = (String) sessionData.get("unionid");

        // 2. Find or create user by openid
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setUnionid(unionid);
            user.setNickname("微信用户");
            user.setAvatarUrl("");
            user.setRole("USER");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
            log.info("Created new user with openid: {}", openid);
        } else {
            // Update unionid if it was missing
            if (unionid != null && (user.getUnionid() == null || user.getUnionid().isBlank())) {
                user.setUnionid(unionid);
            }
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        }

        // 3. Check if user is disabled
        if ("DISABLED".equals(user.getRole())) {
            throw new BizException(ErrorCode.AUTH_USER_DISABLED);
        }

        // 4. Generate JWT
        String token = jwtUtil.generate(user.getId(), user.getRole());

        // 5. Build response
        UserSummary summary = new UserSummary();
        summary.setId(user.getId());
        summary.setNickname(user.getNickname());
        summary.setAvatarUrl(user.getAvatarUrl());

        LoginResponse response = new LoginResponse();
        response.setAccessToken(token);
        response.setExpiresIn(jwtExpirationSeconds);
        response.setUser(summary);

        log.info("User {} ({}) logged in successfully", user.getId(), user.getOpenid());
        return response;
    }
}
