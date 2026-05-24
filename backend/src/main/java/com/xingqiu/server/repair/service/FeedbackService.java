package com.xingqiu.server.repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.repair.domain.Feedback;
import com.xingqiu.server.repair.dto.CreateFeedbackRequest;
import com.xingqiu.server.repair.mapper.FeedbackMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;

    public FeedbackService(FeedbackMapper feedbackMapper) {
        this.feedbackMapper = feedbackMapper;
    }

    public Feedback createFeedback(Long userId, CreateFeedbackRequest request) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setContent(request.getContent());
        feedback.setImages(request.getImages());
        feedback.setStatus("PENDING");
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackMapper.insert(feedback);
        return feedback;
    }

    public Page<Feedback> listFeedbacks(String status, int page, int pageSize) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Feedback::getStatus, status);
        }
        wrapper.orderByDesc(Feedback::getCreatedAt);
        return feedbackMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    public Feedback updateFeedbackStatus(Long id, String status, Long handlerId, String reply) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new RuntimeException("反馈不存在");
        }
        feedback.setStatus(status);
        if (handlerId != null) {
            feedback.setHandlerId(handlerId);
        }
        if (reply != null) {
            feedback.setReply(reply);
        }
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackMapper.updateById(feedback);
        return feedback;
    }
}
