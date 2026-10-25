package com.educonnect.dto;

/**
 * Summary metrics and aggregation counters for the administrator dashboard.
 */
public class AdminDashboardStatsResponse {

    private long totalUsers;
    private long totalResources;
    private long approvedResources;
    private long pendingResources;
    private long rejectedResources;
    private long totalBookmarks;

    public AdminDashboardStatsResponse() {
    }

    public AdminDashboardStatsResponse(long totalUsers, long totalResources, long approvedResources, long pendingResources, long rejectedResources, long totalBookmarks) {
        this.totalUsers = totalUsers;
        this.totalResources = totalResources;
        this.approvedResources = approvedResources;
        this.pendingResources = pendingResources;
        this.rejectedResources = rejectedResources;
        this.totalBookmarks = totalBookmarks;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalResources() {
        return totalResources;
    }

    public void setTotalResources(long totalResources) {
        this.totalResources = totalResources;
    }

    public long getApprovedResources() {
        return approvedResources;
    }

    public void setApprovedResources(long approvedResources) {
        this.approvedResources = approvedResources;
    }

    public long getPendingResources() {
        return pendingResources;
    }

    public void setPendingResources(long pendingResources) {
        this.pendingResources = pendingResources;
    }

    public long getRejectedResources() {
        return rejectedResources;
    }

    public void setRejectedResources(long rejectedResources) {
        this.rejectedResources = rejectedResources;
    }

    public long getTotalBookmarks() {
        return totalBookmarks;
    }

    public void setTotalBookmarks(long totalBookmarks) {
        this.totalBookmarks = totalBookmarks;
    }
}
