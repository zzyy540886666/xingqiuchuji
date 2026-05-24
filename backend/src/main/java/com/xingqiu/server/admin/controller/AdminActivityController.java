package com.xingqiu.server.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.marketing.domain.MarketingActivity;
import com.xingqiu.server.marketing.mapper.MarketingActivityMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/activities")
public class AdminActivityController {

    private final MarketingActivityMapper activityMapper;
    private final ObjectMapper objectMapper;

    public AdminActivityController(MarketingActivityMapper activityMapper, ObjectMapper objectMapper) {
        this.activityMapper = activityMapper;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ApiResponse<PageResult<MarketingActivity>> list(@RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "20") int pageSize,
                                                           @RequestParam(required = false) String status,
                                                           @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<MarketingActivity> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            wrapper.eq(MarketingActivity::getStatus, MarketingActivity.ActivityStatus.valueOf(status));
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(MarketingActivity::getTitle, keyword);
        }
        wrapper.orderByDesc(MarketingActivity::getUpdatedAt);
        Page<MarketingActivity> p = activityMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return ApiResponse.ok(PageResult.of(p.getRecords(), p.getTotal(), page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<MarketingActivity> get(@PathVariable Long id) {
        return ApiResponse.ok(requireActivity(id));
    }

    @PostMapping
    @Transactional
    public ApiResponse<MarketingActivity> create(@RequestBody Map<String, Object> body) {
        MarketingActivity activity = new MarketingActivity();
        apply(activity, body);
        activity.setStatus(activity.getStatus() == null ? MarketingActivity.ActivityStatus.DRAFT : activity.getStatus());
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        activityMapper.insert(activity);
        return ApiResponse.ok(activityMapper.selectById(activity.getId()));
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<MarketingActivity> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        MarketingActivity activity = requireActivity(id);
        apply(activity, body);
        activity.setUpdatedAt(LocalDateTime.now());
        activityMapper.updateById(activity);
        return ApiResponse.ok(activityMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireActivity(id);
        activityMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    private MarketingActivity requireActivity(Long id) {
        MarketingActivity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "activity not found");
        }
        return activity;
    }

    private void apply(MarketingActivity activity, Map<String, Object> body) {
        activity.setTitle(text(body.get("title"), activity.getTitle()));
        activity.setSubtitle(text(body.get("subtitle"), activity.getSubtitle()));
        activity.setTag(text(body.get("tag"), activity.getTag()));
        activity.setCoverUrl(text(body.get("coverUrl"), activity.getCoverUrl()));
        activity.setVideoUrl(text(body.get("videoUrl"), activity.getVideoUrl()));
        activity.setDescription(text(body.get("description"), activity.getDescription()));
        activity.setLinkUrl(text(body.get("linkUrl"), activity.getLinkUrl()));
        activity.setContentJson(jsonOrString(body.getOrDefault("contentJson", body.get("content")), activity.getContentJson()));
        activity.setStartAt(dateTime(body.get("startAt"), activity.getStartAt()));
        activity.setEndAt(dateTime(body.get("endAt"), activity.getEndAt()));
        activity.setSortOrder(number(body.get("sortOrder"), activity.getSortOrder() == null ? 0L : activity.getSortOrder().longValue()).intValue());
        if (body.get("status") != null) {
            activity.setStatus(MarketingActivity.ActivityStatus.valueOf(String.valueOf(body.get("status"))));
        }
    }

    private String text(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private String jsonOrString(Object value, String fallback) {
        if (value == null) return fallback;
        if (value instanceof String s) return s;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Invalid activity content JSON");
        }
    }

    private LocalDateTime dateTime(Object value, LocalDateTime fallback) {
        if (value == null || String.valueOf(value).isBlank()) return fallback;
        return LocalDateTime.parse(String.valueOf(value).replace(" ", "T"));
    }

    private Long number(Object value, Long fallback) {
        if (value == null || String.valueOf(value).isBlank()) return fallback;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(String.valueOf(value));
    }
}
