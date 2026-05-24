package com.xingqiu.server.repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.repair.domain.InspectionTask;
import com.xingqiu.server.repair.domain.WorkOrder;
import com.xingqiu.server.repair.dto.ChartDataResponse;
import com.xingqiu.server.repair.dto.DashboardOverview;
import com.xingqiu.server.repair.mapper.InspectionTaskMapper;
import com.xingqiu.server.repair.mapper.WorkOrderMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final WorkOrderMapper workOrderMapper;
    private final InspectionTaskMapper inspectionTaskMapper;

    public DashboardService(WorkOrderMapper workOrderMapper,
                            InspectionTaskMapper inspectionTaskMapper) {
        this.workOrderMapper = workOrderMapper;
        this.inspectionTaskMapper = inspectionTaskMapper;
    }

    public DashboardOverview getOverview() {
        List<WorkOrder> allOrders = workOrderMapper.selectList(null);
        int totalWorkOrders = allOrders.size();

        int pendingCount = (int) allOrders.stream()
                .filter(o -> "NEW".equals(o.getStatus())
                        || "ASSIGNED".equals(o.getStatus())
                        || "IN_PROGRESS".equals(o.getStatus()))
                .count();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        int completedToday = (int) allOrders.stream()
                .filter(o -> "DONE".equals(o.getStatus()) && o.getCompletedAt() != null
                        && o.getCompletedAt().isAfter(todayStart))
                .count();

        List<InspectionTask> allTasks = inspectionTaskMapper.selectList(null);
        int overdueCount = (int) allTasks.stream()
                .filter(t -> "OVERDUE".equals(t.getStatus()))
                .count();

        long completedTasks = allTasks.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .count();
        double inspectionCompletionRate = allTasks.isEmpty() ? 0 :
                (double) completedTasks / allTasks.size() * 100;

        double avgProcessingHours = 0;
        List<WorkOrder> completedOrders = allOrders.stream()
                .filter(o -> "DONE".equals(o.getStatus())
                        && o.getCreatedAt() != null && o.getCompletedAt() != null)
                .collect(Collectors.toList());
        if (!completedOrders.isEmpty()) {
            double totalHours = completedOrders.stream()
                    .mapToDouble(o -> {
                        Duration d = Duration.between(o.getCreatedAt(), o.getCompletedAt());
                        return d.toMinutes() / 60.0;
                    })
                    .sum();
            avgProcessingHours = Math.round(totalHours / completedOrders.size() * 10.0) / 10.0;
        }

        DashboardOverview overview = new DashboardOverview();
        overview.setTotalWorkOrders(totalWorkOrders);
        overview.setPendingWorkOrders(pendingCount);
        overview.setCompletedToday(completedToday);
        overview.setOverdueCount(overdueCount);
        overview.setInspectionCompletionRate(Math.round(inspectionCompletionRate * 10.0) / 10.0);
        overview.setAvgProcessingHours(avgProcessingHours);
        return overview;
    }

    public Map<String, Object> getWorkOrderStats(LocalDateTime startDate, LocalDateTime endDate, String area) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            wrapper.ge(WorkOrder::getCreatedAt, startDate);
        }
        if (endDate != null) {
            wrapper.le(WorkOrder::getCreatedAt, endDate);
        }
        List<WorkOrder> orders = workOrderMapper.selectList(wrapper);

        Map<String, Long> statusCounts = orders.stream()
                .collect(Collectors.groupingBy(WorkOrder::getStatus, Collectors.counting()));

        Map<String, Long> faultTypeCounts = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getFaultType() != null ? o.getFaultType() : "其他",
                        Collectors.counting()));

        Map<String, Object> result = new HashMap<>();
        result.put("statusCounts", statusCounts);
        result.put("faultTypeCounts", faultTypeCounts);
        return result;
    }

    public Map<String, Object> getInspectionStats(LocalDateTime startDate, LocalDateTime endDate, String area) {
        LambdaQueryWrapper<InspectionTask> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            wrapper.ge(InspectionTask::getCreatedAt, startDate);
        }
        if (endDate != null) {
            wrapper.le(InspectionTask::getCreatedAt, endDate);
        }
        List<InspectionTask> tasks = inspectionTaskMapper.selectList(wrapper);

        int total = tasks.size();
        long completed = tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        long overdue = tasks.stream().filter(t -> "OVERDUE".equals(t.getStatus())).count();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("completed", completed);
        result.put("overdue", overdue);
        result.put("completionRate", total > 0 ? Math.round((double) completed / total * 1000.0) / 10.0 : 0);
        result.put("overdueRate", total > 0 ? Math.round((double) overdue / total * 1000.0) / 10.0 : 0);
        return result;
    }

    public List<Map<String, Object>> getEfficiencyStats(LocalDateTime startDate, LocalDateTime endDate) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkOrder::getStatus, "DONE");
        if (startDate != null) {
            wrapper.ge(WorkOrder::getCompletedAt, startDate);
        }
        if (endDate != null) {
            wrapper.le(WorkOrder::getCompletedAt, endDate);
        }
        List<WorkOrder> completedOrders = workOrderMapper.selectList(wrapper);

        Map<Long, List<WorkOrder>> byTechnician = completedOrders.stream()
                .filter(o -> o.getAssignedTechnicianId() != null)
                .collect(Collectors.groupingBy(WorkOrder::getAssignedTechnicianId));

        List<Map<String, Object>> rankings = new ArrayList<>();
        for (Map.Entry<Long, List<WorkOrder>> entry : byTechnician.entrySet()) {
            double avgHours = entry.getValue().stream()
                    .mapToDouble(o -> {
                        if (o.getCreatedAt() != null && o.getCompletedAt() != null) {
                            return Duration.between(o.getCreatedAt(), o.getCompletedAt()).toMinutes() / 60.0;
                        }
                        return 0;
                    })
                    .average()
                    .orElse(0);

            Map<String, Object> ranking = new HashMap<>();
            ranking.put("technicianId", entry.getKey());
            ranking.put("completedCount", entry.getValue().size());
            ranking.put("avgProcessingHours", Math.round(avgHours * 10.0) / 10.0);
            rankings.add(ranking);
        }

        rankings.sort((a, b) -> Double.compare(
                (Double) b.get("avgProcessingHours"), (Double) a.get("avgProcessingHours")));

        return rankings;
    }

    public ChartDataResponse getChartData(String chartType, LocalDateTime startDate, LocalDateTime endDate) {
        ChartDataResponse response = new ChartDataResponse();
        response.setName(chartType);

        switch (chartType != null ? chartType : "") {
            case "trend": {
                List<Map<String, Object>> data = buildTrendData(startDate, endDate);
                response.setData(data);
                break;
            }
            case "ranking": {
                response.setData(getEfficiencyStats(startDate, endDate));
                break;
            }
            case "area-comparison": {
                response.setData(buildAreaComparisonData(startDate, endDate));
                break;
            }
            default:
                response.setData(Collections.emptyList());
        }
        return response;
    }

    private List<Map<String, Object>> buildTrendData(LocalDateTime startDate, LocalDateTime endDate) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            wrapper.ge(WorkOrder::getCreatedAt, startDate);
        }
        if (endDate != null) {
            wrapper.le(WorkOrder::getCreatedAt, endDate);
        }
        List<WorkOrder> orders = workOrderMapper.selectList(wrapper);

        Map<LocalDate, Long> dailyNew = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().toLocalDate(),
                        TreeMap::new, Collectors.counting()
                ));

        Map<LocalDate, Long> dailyDone = orders.stream()
                .filter(o -> "DONE".equals(o.getStatus()) && o.getCompletedAt() != null)
                .collect(Collectors.groupingBy(
                        o -> o.getCompletedAt().toLocalDate(),
                        TreeMap::new, Collectors.counting()
                ));

        List<Map<String, Object>> data = new ArrayList<>();
        for (LocalDate date : dailyNew.keySet()) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", date.toString());
            point.put("newCount", dailyNew.getOrDefault(date, 0L));
            point.put("doneCount", dailyDone.getOrDefault(date, 0L));
            data.add(point);
        }
        return data;
    }

    private List<Map<String, Object>> buildAreaComparisonData(LocalDateTime startDate, LocalDateTime endDate) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            wrapper.ge(WorkOrder::getCreatedAt, startDate);
        }
        if (endDate != null) {
            wrapper.le(WorkOrder::getCreatedAt, endDate);
        }
        List<WorkOrder> orders = workOrderMapper.selectList(wrapper);

        Map<String, Map<String, Long>> areaStats = new HashMap<>();
        for (WorkOrder order : orders) {
            String deviceName = order.getDeviceName() != null ? order.getDeviceName() : "未知区域";
            areaStats.computeIfAbsent(deviceName, k -> new HashMap<>())
                    .merge(order.getStatus(), 1L, Long::sum);
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (Map.Entry<String, Map<String, Long>> entry : areaStats.entrySet()) {
            Map<String, Object> area = new HashMap<>();
            area.put("area", entry.getKey());
            area.put("counts", entry.getValue());
            long total = entry.getValue().values().stream().mapToLong(Long::longValue).sum();
            area.put("total", total);
            data.add(area);
        }
        return data;
    }
}
