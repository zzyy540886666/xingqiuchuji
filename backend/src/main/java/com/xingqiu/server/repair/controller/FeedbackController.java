package com.xingqiu.server.repair.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import com.xingqiu.server.common.response.PageResult;
import com.xingqiu.server.repair.domain.Feedback;
import com.xingqiu.server.repair.dto.CreateFeedbackRequest;
import com.xingqiu.server.repair.service.FeedbackService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ApiResponse<Feedback> createFeedback(@RequestBody CreateFeedbackRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        Feedback feedback = feedbackService.createFeedback(userId, request);
        return ApiResponse.ok(feedback);
    }

    @GetMapping
    public ApiResponse<PageResult<Feedback>> listFeedbacks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        requireAdmin();
        Page<Feedback> result = feedbackService.listFeedbacks(status, page, pageSize);
        return ApiResponse.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @PatchMapping("/{id}")
    public ApiResponse<Feedback> updateFeedbackStatus(@PathVariable Long id,
                                                       @RequestBody Map<String, Object> body) {
        requireAdmin();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long handlerId = (Long) auth.getPrincipal();
        String status = (String) body.get("status");
        String reply = (String) body.get("reply");
        Feedback feedback = feedbackService.updateFeedbackStatus(id, status, handlerId, reply);
        return ApiResponse.ok(feedback);
    }

    private void requireAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
    }
}
