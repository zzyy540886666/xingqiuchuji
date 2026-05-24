package com.xingqiu.server.repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.repair.domain.InspectionPlan;
import com.xingqiu.server.repair.domain.InspectionRecord;
import com.xingqiu.server.repair.domain.InspectionTask;
import com.xingqiu.server.repair.domain.InspectionTemplate;
import com.xingqiu.server.repair.dto.CreateInspectionPlanRequest;
import com.xingqiu.server.repair.dto.InspectionSubmitRequest;
import com.xingqiu.server.repair.mapper.InspectionPlanMapper;
import com.xingqiu.server.repair.mapper.InspectionRecordMapper;
import com.xingqiu.server.repair.mapper.InspectionTaskMapper;
import com.xingqiu.server.repair.mapper.InspectionTemplateMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InspectionService {

    private final InspectionTemplateMapper templateMapper;
    private final InspectionPlanMapper planMapper;
    private final InspectionTaskMapper taskMapper;
    private final InspectionRecordMapper recordMapper;
    private final NotificationService notificationService;

    public InspectionService(InspectionTemplateMapper templateMapper,
                             InspectionPlanMapper planMapper,
                             InspectionTaskMapper taskMapper,
                             InspectionRecordMapper recordMapper,
                             NotificationService notificationService) {
        this.templateMapper = templateMapper;
        this.planMapper = planMapper;
        this.taskMapper = taskMapper;
        this.recordMapper = recordMapper;
        this.notificationService = notificationService;
    }

    // --- Templates ---

    public Page<InspectionTemplate> listTemplates(int page, int pageSize) {
        LambdaQueryWrapper<InspectionTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(InspectionTemplate::getCreatedAt);
        return templateMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    public InspectionTemplate createTemplate(InspectionTemplate template) {
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        templateMapper.insert(template);
        return template;
    }

    // --- Plans ---

    public Page<InspectionPlan> listPlans(String status, int page, int pageSize) {
        LambdaQueryWrapper<InspectionPlan> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(InspectionPlan::getStatus, status);
        }
        wrapper.orderByDesc(InspectionPlan::getCreatedAt);
        return planMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    public InspectionPlan createPlan(CreateInspectionPlanRequest request) {
        InspectionPlan plan = new InspectionPlan();
        plan.setTemplateId(request.getTemplateId());
        plan.setName(request.getName());
        plan.setArea(request.getArea());
        plan.setFrequency(request.getFrequency());
        plan.setAssignedTechnicianId(request.getAssignedTechnicianId());
        plan.setStatus("ACTIVE");
        plan.setNextRunAt(computeNextRunAt(request.getFrequency()));
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        planMapper.insert(plan);
        return plan;
    }

    public InspectionPlan updatePlan(Long id, CreateInspectionPlanRequest request) {
        InspectionPlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new BizException(ErrorCode.INSPECTION_TASK_NOT_FOUND, "巡检计划不存在");
        }
        plan.setTemplateId(request.getTemplateId());
        plan.setName(request.getName());
        plan.setArea(request.getArea());
        plan.setFrequency(request.getFrequency());
        plan.setAssignedTechnicianId(request.getAssignedTechnicianId());
        plan.setUpdatedAt(LocalDateTime.now());
        planMapper.updateById(plan);
        return plan;
    }

    // --- Tasks ---

    public Page<InspectionTask> listTasks(Long userId, boolean isAdmin, boolean isTech,
                                          String status, int page, int pageSize) {
        LambdaQueryWrapper<InspectionTask> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(InspectionTask::getStatus, status);
        }
        if (!isAdmin && isTech) {
            wrapper.eq(InspectionTask::getAssignedTechnicianId, userId);
        }
        wrapper.orderByDesc(InspectionTask::getCreatedAt);
        return taskMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    public InspectionTask startTask(Long id, Long technicianId) {
        InspectionTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException(ErrorCode.INSPECTION_TASK_NOT_FOUND);
        }
        if (task.getAssignedTechnicianId() != null &&
                !task.getAssignedTechnicianId().equals(technicianId)) {
            throw new BizException(ErrorCode.WORK_ORDER_ACTION_DENIED, "该巡检任务未分配给您");
        }
        if (!"PENDING".equals(task.getStatus())) {
            throw new BizException(ErrorCode.WORK_ORDER_ACTION_DENIED, "只有待执行的巡检任务才能开始");
        }
        task.setStatus(InspectionTask.InspectionStatus.IN_PROGRESS.name());
        task.setStartedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        return task;
    }

    public void submitTask(Long id, Long technicianId, InspectionSubmitRequest request) {
        InspectionTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException(ErrorCode.INSPECTION_TASK_NOT_FOUND);
        }
        if (task.getAssignedTechnicianId() != null &&
                !task.getAssignedTechnicianId().equals(technicianId)) {
            throw new BizException(ErrorCode.WORK_ORDER_ACTION_DENIED, "该巡检任务未分配给您");
        }
        if (!InspectionTask.InspectionStatus.IN_PROGRESS.name().equals(task.getStatus())) {
            throw new BizException(ErrorCode.WORK_ORDER_ACTION_DENIED, "只有进行中的巡检任务才能提交");
        }
        LocalDateTime now = LocalDateTime.now();

        if (request.getRecords() != null) {
            for (InspectionSubmitRequest.RecordItem item : request.getRecords()) {
                InspectionRecord record = new InspectionRecord();
                record.setTaskId(id);
                record.setCheckItem(item.getItemName());
                record.setResult(item.getResult());
                record.setIssueDescription(item.getIssueDescription());
                record.setImages(item.getImages());
                record.setRecordedAt(now);
                recordMapper.insert(record);
            }
        }

        task.setStatus(InspectionTask.InspectionStatus.COMPLETED.name());
        task.setCompletedAt(now);
        taskMapper.updateById(task);
    }

    public Map<String, Object> getTaskReportSummary(String area, LocalDateTime startDate, LocalDateTime endDate) {
        LambdaQueryWrapper<InspectionTask> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            wrapper.ge(InspectionTask::getCreatedAt, startDate);
        }
        if (endDate != null) {
            wrapper.le(InspectionTask::getCreatedAt, endDate);
        }
        List<InspectionTask> tasks = taskMapper.selectList(wrapper);

        int total = tasks.size();
        int completed = (int) tasks.stream()
                .filter(t -> InspectionTask.InspectionStatus.COMPLETED.name().equals(t.getStatus()))
                .count();
        int overdue = (int) tasks.stream()
                .filter(t -> InspectionTask.InspectionStatus.OVERDUE.name().equals(t.getStatus()))
                .count();
        double completionRate = total > 0 ? (double) completed / total * 100 : 0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("total", total);
        summary.put("completed", completed);
        summary.put("overdue", overdue);
        summary.put("completionRate", Math.round(completionRate * 10.0) / 10.0);
        return summary;
    }

    private LocalDateTime computeNextRunAt(String frequency) {
        LocalDateTime now = LocalDateTime.now();
        switch (frequency) {
            case "DAILY":
                return now.plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
            case "WEEKLY":
                return now.plusWeeks(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
            case "MONTHLY":
                return now.plusMonths(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
            default:
                return now.plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
        }
    }
}
