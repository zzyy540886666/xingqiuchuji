package com.xingqiu.server.auth.service;

import com.xingqiu.server.auth.domain.User;
import com.xingqiu.server.auth.dto.UserSummary;
import com.xingqiu.server.auth.mapper.UserMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.member.dto.MembershipResponse;
import com.xingqiu.server.member.service.MemberService;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserMapper userMapper;
    private final MemberService memberService;

    public CurrentUserService(UserMapper userMapper, MemberService memberService) {
        this.userMapper = userMapper;
        this.memberService = memberService;
    }

    public UserSummary getSummary(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        MembershipResponse membership = memberService.getMembership(userId);
        UserSummary summary = new UserSummary();
        summary.setId(user.getId());
        summary.setNickname(user.getNickname());
        summary.setAvatarUrl(user.getAvatarUrl());
        summary.setMembershipLevel(membership.getLevel());
        summary.setIsNativeResident(Boolean.TRUE.equals(membership.getIsNative()));
        return summary;
    }
}
