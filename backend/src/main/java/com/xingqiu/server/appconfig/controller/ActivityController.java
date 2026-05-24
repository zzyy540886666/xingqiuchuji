package com.xingqiu.server.appconfig.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.marketing.domain.MarketingActivity;
import com.xingqiu.server.marketing.mapper.MarketingActivityMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/config/activities")
public class ActivityController {

    private final MarketingActivityMapper activityMapper;

    public ActivityController(MarketingActivityMapper activityMapper) {
        this.activityMapper = activityMapper;
    }

    @GetMapping
    public ApiResponse<List<MarketingActivity>> listPublished() {
        LocalDateTime now = LocalDateTime.now();
        List<MarketingActivity> activities = activityMapper.selectList(new LambdaQueryWrapper<MarketingActivity>()
                .eq(MarketingActivity::getStatus, MarketingActivity.ActivityStatus.PUBLISHED)
                .le(MarketingActivity::getStartAt, now)
                .and(w -> w.isNull(MarketingActivity::getEndAt).or().ge(MarketingActivity::getEndAt, now))
                .orderByAsc(MarketingActivity::getSortOrder));
        return ApiResponse.ok(activities);
    }

    @GetMapping("/{id}")
    public ApiResponse<MarketingActivity> getPublished(@PathVariable Long id) {
        MarketingActivity activity = activityMapper.selectById(id);
        LocalDateTime now = LocalDateTime.now();
        if (activity == null || activity.getStatus() != MarketingActivity.ActivityStatus.PUBLISHED
                || activity.getStartAt() == null || activity.getStartAt().isAfter(now)
                || (activity.getEndAt() != null && activity.getEndAt().isBefore(now))) {
            throw new BizException(ErrorCode.NOT_FOUND, "activity not found");
        }
        return ApiResponse.ok(activity);
    }
}
