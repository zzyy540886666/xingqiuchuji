package com.xingqiu.server.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.admin.domain.AdminUser;
import com.xingqiu.server.admin.dto.AdminCurrentResponse;
import com.xingqiu.server.admin.dto.AdminLoginRequest;
import com.xingqiu.server.admin.dto.AdminLoginResponse;
import com.xingqiu.server.admin.mapper.AdminUserMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminAuthService {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthService.class);

    private final AdminUserMapper adminUserMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AdminAuthService(AdminUserMapper adminUserMapper, JwtUtil jwtUtil,
                            PasswordEncoder passwordEncoder) {
        this.adminUserMapper = adminUserMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public AdminLoginResponse login(AdminLoginRequest request) {
        LambdaQueryWrapper<AdminUser> query = new LambdaQueryWrapper<>();
        query.eq(AdminUser::getUsername, request.getUsername());
        AdminUser admin = adminUserMapper.selectOne(query);

        if (admin == null || Boolean.FALSE.equals(admin.getEnabled())) {
            log.warn("Admin login failed: username={} not found or disabled", request.getUsername());
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            log.warn("Admin login failed: username={} wrong password", request.getUsername());
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        String token = jwtUtil.generate(admin.getId(), admin.getRole());

        admin.setLastLoginAt(LocalDateTime.now());
        adminUserMapper.updateById(admin);

        AdminLoginResponse resp = new AdminLoginResponse();
        resp.setAccessToken(token);
        resp.setExpiresIn(7200L);
        resp.setDisplayName(admin.getDisplayName());
        resp.setRole(admin.getRole());

        log.info("Admin login success: username={}", request.getUsername());
        return resp;
    }

    public AdminCurrentResponse currentAdmin(Long adminId) {
        AdminUser admin = adminUserMapper.selectById(adminId);
        if (admin == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        if (Boolean.FALSE.equals(admin.getEnabled())) {
            throw new BizException(ErrorCode.AUTH_USER_DISABLED);
        }

        AdminCurrentResponse resp = new AdminCurrentResponse();
        resp.setId(admin.getId());
        resp.setUsername(admin.getUsername());
        resp.setDisplayName(admin.getDisplayName());
        resp.setRoles(List.of(admin.getRole()));
        resp.setPermissions(List.of());
        return resp;
    }
}
