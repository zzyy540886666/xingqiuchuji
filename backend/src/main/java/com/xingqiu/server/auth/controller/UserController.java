package com.xingqiu.server.auth.controller;

import com.xingqiu.server.auth.domain.User;
import com.xingqiu.server.auth.dto.UpdateUserRequest;
import com.xingqiu.server.auth.dto.UserSummary;
import com.xingqiu.server.auth.mapper.UserMapper;
import com.xingqiu.server.auth.service.CurrentUserService;
import com.xingqiu.server.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final CurrentUserService currentUserService;
    private final UserMapper userMapper;

    public UserController(CurrentUserService currentUserService, UserMapper userMapper) {
        this.currentUserService = currentUserService;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public ApiResponse<UserSummary> getCurrentUser() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ApiResponse.ok(currentUserService.getSummary(userId));
    }

    @PutMapping("/me")
    public ApiResponse<UserSummary> updateCurrentUser(@Valid @RequestBody UpdateUserRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ApiResponse.fail("USER_NOT_FOUND", "用户不存在");
        }
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            user.setNickname(request.getNickname().trim());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }
        userMapper.updateById(user);
        return ApiResponse.ok(currentUserService.getSummary(userId));
    }
}
