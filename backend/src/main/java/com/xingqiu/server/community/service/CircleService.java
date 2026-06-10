package com.xingqiu.server.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.community.domain.*;
import com.xingqiu.server.community.dto.PostResponse;
import com.xingqiu.server.community.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CircleService {

    private static final Logger log = LoggerFactory.getLogger(CircleService.class);

    private final CircleMapper circleMapper;
    private final CircleMemberMapper circleMemberMapper;
    private final PostMapper postMapper;
    private final PostService postService;

    public CircleService(CircleMapper circleMapper,
                         CircleMemberMapper circleMemberMapper,
                         PostMapper postMapper,
                         PostService postService) {
        this.circleMapper = circleMapper;
        this.circleMemberMapper = circleMemberMapper;
        this.postMapper = postMapper;
        this.postService = postService;
    }

    /**
     * List all circles.
     */
    public List<Circle> listCircles() {
        LambdaQueryWrapper<Circle> query = new LambdaQueryWrapper<>();
        query.orderByDesc(Circle::getMemberCount);
        return circleMapper.selectList(query);
    }

    /**
     * Join a circle.
     */
    @Transactional
    public void joinCircle(Long userId, Long circleId) {
        Circle circle = circleMapper.selectById(circleId);
        if (circle == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "圈子不存在");
        }

        // Check if already a member
        LambdaQueryWrapper<CircleMember> query = new LambdaQueryWrapper<>();
        query.eq(CircleMember::getCircleId, circleId)
                .eq(CircleMember::getUserId, userId);
        if (circleMemberMapper.selectCount(query) > 0) {
            log.info("userId={} already member of circleId={}", userId, circleId);
            return;
        }

        // Add member
        CircleMember member = new CircleMember();
        member.setCircleId(circleId);
        member.setUserId(userId);
        member.setRole("MEMBER");
        member.setJoinedAt(LocalDateTime.now());
        circleMemberMapper.insert(member);

        // Update member count
        circle.setMemberCount((circle.getMemberCount() != null ? circle.getMemberCount() : 0) + 1);
        circleMapper.updateById(circle);

        log.info("userId={} joined circleId={}", userId, circleId);
    }

    /**
     * Get feed for a specific circle.
     */
    public List<PostResponse> getCircleFeed(Long circleId, String cursor, int limit) {
        Circle circle = circleMapper.selectById(circleId);
        if (circle == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "圈子不存在");
        }

        // Reuse post feed logic, filtered by circle
        LambdaQueryWrapper<Post> query = new LambdaQueryWrapper<>();
        query.eq(Post::getStatus, PostStatus.APPROVED.name())
                .eq(Post::getCircleId, circleId);

        if (cursor != null && !cursor.isEmpty()) {
            try {
                LocalDateTime cursorTime = LocalDateTime.parse(cursor);
                query.lt(Post::getCreatedAt, cursorTime);
            } catch (Exception e) {
                log.warn("Invalid cursor format: {}", cursor);
            }
        }
        query.orderByDesc(Post::getCreatedAt);

        Page<Post> page = new Page<>(1, Math.max(1, Math.min(limit, 50)));
        List<Post> posts = postMapper.selectPage(page, query).getRecords();

        // Convert using postService pattern - manual conversion for simplicity
        java.util.ArrayList<PostResponse> result = new java.util.ArrayList<>();
        for (Post post : posts) {
            result.add(postService.getPostDetail(post.getId(), null));
        }
        return result;
    }
}
