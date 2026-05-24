package com.xingqiu.server.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.auth.domain.User;
import com.xingqiu.server.auth.mapper.UserMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.PageResult;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    private final UserMapper userMapper;

    public AdminUserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public PageResult<User> listUsers(String keyword, String role, int page, int pageSize) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            query.and(w -> w.like(User::getNickname, keyword)
                    .or().like(User::getOpenid, keyword));
        }
        if (role != null && !role.isBlank()) {
            query.eq(User::getRole, role);
        }
        query.orderByDesc(User::getCreatedAt);

        Page<User> pageObj = new Page<>(page, pageSize);
        Page<User> result = userMapper.selectPage(pageObj, query);
        return PageResult.of(result.getRecords(), result.getTotal(), page, pageSize);
    }

    public User getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    public void updateUserRole(Long userId, String newRole) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (!"USER".equals(newRole) && !"TECHNICIAN".equals(newRole) && !"ADMIN".equals(newRole)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "无效的角色: " + newRole);
        }
        user.setRole(newRole);
        userMapper.updateById(user);
    }

    public void toggleUserStatus(Long userId, boolean enable) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (enable) {
            user.setRole(user.getRole() == null || user.getRole().equals("DISABLED") ? "USER" : user.getRole());
        } else {
            user.setRole("DISABLED");
        }
        userMapper.updateById(user);
    }
}
