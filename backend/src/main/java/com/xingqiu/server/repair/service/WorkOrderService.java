package com.xingqiu.server.repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.repair.domain.WorkOrder;
import com.xingqiu.server.repair.domain.WorkOrderLog;
import com.xingqiu.server.repair.dto.CreateWorkOrderRequest;
import com.xingqiu.server.repair.dto.WorkOrderProgressRequest;
import com.xingqiu.server.repair.mapper.WorkOrderLogMapper;
import com.xingqiu.server.repair.mapper.WorkOrderMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderLogMapper workOrderLogMapper;
    private final NotificationService notificationService;

    public WorkOrderService(WorkOrderMapper workOrderMapper,
                            WorkOrderLogMapper workOrderLogMapper,
                            NotificationService notificationService) {
        this.workOrderMapper = workOrderMapper;
        this.workOrderLogMapper = workOrderLogMapper;
        this.notificationService = notificationService;
    }

    public WorkOrder createWorkOrder(Long userId, CreateWorkOrderRequest request) {
        WorkOrder order = new WorkOrder();
        order.setOrderNo("WO" + System.currentTimeMillis());
        order.setDeviceId(request.getDeviceId());
        order.setReporterUserId(userId);
        order.setReporterName(request.getReporterName());
        order.setReporterPhone(request.getReporterPhone());
        order.setFaultType(request.getFaultType());
        order.setFaultDescription(request.getFaultDescription());
        order.setImages(request.getImages());
        order.setPriority("MEDIUM");
        order.setStatus(WorkOrder.WorkOrderStatus.NEW.name());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        workOrderMapper.insert(order);

        logWorkOrder(order.getId(), "CREATED", null, order.getStatus(), userId, request.getReporterName(), null);
        return order;
    }

    public Page<WorkOrder> listWorkOrders(Long userId, boolean isAdmin, boolean isTech,
                                          String status, int page, int pageSize) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(WorkOrder::getStatus, status);
        }
        if (isAdmin) {
            // admin sees all
        } else if (isTech) {
            wrapper.eq(WorkOrder::getAssignedTechnicianId, userId);
        } else {
            wrapper.eq(WorkOrder::getReporterUserId, userId);
        }
        wrapper.orderByDesc(WorkOrder::getCreatedAt);
        return workOrderMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    public WorkOrder getWorkOrder(Long id) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BizException(ErrorCode.WORK_ORDER_NOT_FOUND);
        }
        return order;
    }

    public WorkOrder getReadableWorkOrder(Long id, Long userId, boolean isAdmin, boolean isTech) {
        WorkOrder order = getWorkOrder(id);
        boolean ownsReport = userId.equals(order.getReporterUserId());
        boolean assigned = userId.equals(order.getAssignedTechnicianId());
        if (!isAdmin && !(isTech && assigned) && !ownsReport) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return order;
    }

    public void assignWorkOrder(Long id, Long technicianId, Long operatorId) {
        WorkOrder order = getWorkOrder(id);
        if (!WorkOrder.WorkOrderStatus.NEW.name().equals(order.getStatus())) {
            throw new BizException(ErrorCode.WORK_ORDER_ACTION_DENIED, "只有新建状态的工单才能分配");
        }
        String fromStatus = order.getStatus();
        order.setStatus(WorkOrder.WorkOrderStatus.ASSIGNED.name());
        order.setAssignedTechnicianId(technicianId);
        order.setUpdatedAt(LocalDateTime.now());
        workOrderMapper.updateById(order);

        logWorkOrder(id, "ASSIGNED", fromStatus, order.getStatus(), operatorId, null, null);
        notificationService.sendNotification(technicianId, "NEW_WORK_ORDER",
                "新工单分配", "您有一个新的维修工单待处理", String.valueOf(id));
    }

    public void acceptWorkOrder(Long id, Long technicianId) {
        WorkOrder order = getWorkOrder(id);
        if (order.getAssignedTechnicianId() == null ||
                !order.getAssignedTechnicianId().equals(technicianId)) {
            throw new BizException(ErrorCode.WORK_ORDER_ACTION_DENIED, "该工单未分配给您");
        }
        if (!WorkOrder.WorkOrderStatus.ASSIGNED.name().equals(order.getStatus())) {
            throw new BizException(ErrorCode.WORK_ORDER_ACTION_DENIED, "只有已分配状态的工单才能接单");
        }
        String fromStatus = order.getStatus();
        order.setStatus(WorkOrder.WorkOrderStatus.IN_PROGRESS.name());
        order.setUpdatedAt(LocalDateTime.now());
        workOrderMapper.updateById(order);

        logWorkOrder(id, "ACCEPTED", fromStatus, order.getStatus(), technicianId, null, null);
    }

    public void rejectWorkOrder(Long id, Long technicianId, String reason) {
        WorkOrder order = getWorkOrder(id);
        if (order.getAssignedTechnicianId() == null ||
                !order.getAssignedTechnicianId().equals(technicianId)) {
            throw new BizException(ErrorCode.WORK_ORDER_ACTION_DENIED, "该工单未分配给您");
        }
        if (!WorkOrder.WorkOrderStatus.ASSIGNED.name().equals(order.getStatus())) {
            throw new BizException(ErrorCode.WORK_ORDER_ACTION_DENIED, "只有已分配状态的工单才能拒绝");
        }
        String fromStatus = order.getStatus();
        order.setStatus(WorkOrder.WorkOrderStatus.REJECTED.name());
        order.setUpdatedAt(LocalDateTime.now());
        workOrderMapper.updateById(order);

        logWorkOrder(id, "REJECTED", fromStatus, order.getStatus(), technicianId, reason, null);
    }

    public void submitProgress(Long id, Long technicianId, WorkOrderProgressRequest request) {
        WorkOrder order = getWorkOrder(id);
        if (order.getAssignedTechnicianId() == null ||
                !order.getAssignedTechnicianId().equals(technicianId)) {
            throw new BizException(ErrorCode.WORK_ORDER_ACTION_DENIED, "该工单未分配给您");
        }
        if (!WorkOrder.WorkOrderStatus.IN_PROGRESS.name().equals(order.getStatus())) {
            throw new BizException(ErrorCode.WORK_ORDER_ACTION_DENIED, "只有进行中状态的工单才能提交进度");
        }
        logWorkOrder(id, "PROGRESS", order.getStatus(), order.getStatus(),
                technicianId, request.getDescription(), null);
    }

    public void completeWorkOrder(Long id, Long technicianId, WorkOrderProgressRequest request) {
        WorkOrder order = getWorkOrder(id);
        if (order.getAssignedTechnicianId() == null ||
                !order.getAssignedTechnicianId().equals(technicianId)) {
            throw new BizException(ErrorCode.WORK_ORDER_ACTION_DENIED, "该工单未分配给您");
        }
        if (!WorkOrder.WorkOrderStatus.IN_PROGRESS.name().equals(order.getStatus())) {
            throw new BizException(ErrorCode.WORK_ORDER_ACTION_DENIED, "只有进行中状态的工单才能提交完成");
        }
        String fromStatus = order.getStatus();
        order.setStatus(WorkOrder.WorkOrderStatus.PENDING_ACCEPT.name());
        order.setSolutionDescription(request.getDescription());
        if (request.getImages() != null) {
            order.setImages(request.getImages());
        }
        order.setCompletedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        workOrderMapper.updateById(order);

        logWorkOrder(id, "COMPLETED", fromStatus, order.getStatus(),
                technicianId, request.getDescription(), null);
    }

    public void verifyAndClose(Long id, Long adminId) {
        WorkOrder order = getWorkOrder(id);
        if (!WorkOrder.WorkOrderStatus.PENDING_ACCEPT.name().equals(order.getStatus())) {
            throw new BizException(ErrorCode.WORK_ORDER_ACTION_DENIED, "只有待验收状态的工单才能验收");
        }
        String fromStatus = order.getStatus();
        order.setStatus(WorkOrder.WorkOrderStatus.DONE.name());
        order.setUpdatedAt(LocalDateTime.now());
        workOrderMapper.updateById(order);

        logWorkOrder(id, "COMPLETED", fromStatus, order.getStatus(), adminId, "管理员验收通过", null);
    }

    public void closeWorkOrder(Long id, Long operatorId) {
        WorkOrder order = getWorkOrder(id);
        if (WorkOrder.WorkOrderStatus.DONE.name().equals(order.getStatus())
                || WorkOrder.WorkOrderStatus.REJECTED.name().equals(order.getStatus())
                || WorkOrder.WorkOrderStatus.CLOSED.name().equals(order.getStatus())) {
            throw new BizException(ErrorCode.WORK_ORDER_ACTION_DENIED, "该工单已是终态，无法关闭");
        }
        String fromStatus = order.getStatus();
        order.setStatus(WorkOrder.WorkOrderStatus.CLOSED.name());
        order.setUpdatedAt(LocalDateTime.now());
        workOrderMapper.updateById(order);

        logWorkOrder(id, "CLOSED", fromStatus, order.getStatus(), operatorId, null, null);
    }

    public List<WorkOrderLog> getWorkOrderLogs(Long id) {
        WorkOrder order = getWorkOrder(id); // validates existence
        LambdaQueryWrapper<WorkOrderLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkOrderLog::getWorkOrderId, id);
        wrapper.orderByAsc(WorkOrderLog::getCreatedAt);
        return workOrderLogMapper.selectList(wrapper);
    }

    private void logWorkOrder(Long workOrderId, String action, String fromStatus,
                              String toStatus, Long operatorId, String remark, String operatorName) {
        WorkOrderLog log = new WorkOrderLog();
        log.setWorkOrderId(workOrderId);
        log.setAction(action);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName != null ? operatorName : String.valueOf(operatorId));
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now());
        workOrderLogMapper.insert(log);
    }
}
