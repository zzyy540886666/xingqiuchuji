package com.xingqiu.server.repair.dto;

public class DashboardOverview {

    private int totalWorkOrders;
    private int pendingWorkOrders;
    private int completedToday;
    private int overdueCount;
    private double inspectionCompletionRate;
    private double avgProcessingHours;

    public int getTotalWorkOrders() { return totalWorkOrders; }
    public void setTotalWorkOrders(int totalWorkOrders) { this.totalWorkOrders = totalWorkOrders; }

    public int getPendingWorkOrders() { return pendingWorkOrders; }
    public void setPendingWorkOrders(int pendingWorkOrders) { this.pendingWorkOrders = pendingWorkOrders; }

    public int getCompletedToday() { return completedToday; }
    public void setCompletedToday(int completedToday) { this.completedToday = completedToday; }

    public int getOverdueCount() { return overdueCount; }
    public void setOverdueCount(int overdueCount) { this.overdueCount = overdueCount; }

    public double getInspectionCompletionRate() { return inspectionCompletionRate; }
    public void setInspectionCompletionRate(double inspectionCompletionRate) { this.inspectionCompletionRate = inspectionCompletionRate; }

    public double getAvgProcessingHours() { return avgProcessingHours; }
    public void setAvgProcessingHours(double avgProcessingHours) { this.avgProcessingHours = avgProcessingHours; }
}
