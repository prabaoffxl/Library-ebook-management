package com.library.dto.report;

import java.util.ArrayList;
import java.util.List;

public class UserReportDTO {

    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long suspendedUsers;
    private List<LabelValueDTO> usersByRole = new ArrayList<>();
    private List<LabelValueDTO> usersByLibrary = new ArrayList<>();
    private List<LabelValueDTO> activityByType = new ArrayList<>();

    public UserReportDTO() {
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getInactiveUsers() {
        return inactiveUsers;
    }

    public void setInactiveUsers(long inactiveUsers) {
        this.inactiveUsers = inactiveUsers;
    }

    public long getSuspendedUsers() {
        return suspendedUsers;
    }

    public void setSuspendedUsers(long suspendedUsers) {
        this.suspendedUsers = suspendedUsers;
    }

    public List<LabelValueDTO> getUsersByRole() {
        return usersByRole;
    }

    public void setUsersByRole(List<LabelValueDTO> usersByRole) {
        this.usersByRole = usersByRole;
    }

    public List<LabelValueDTO> getUsersByLibrary() {
        return usersByLibrary;
    }

    public void setUsersByLibrary(List<LabelValueDTO> usersByLibrary) {
        this.usersByLibrary = usersByLibrary;
    }

    public List<LabelValueDTO> getActivityByType() {
        return activityByType;
    }

    public void setActivityByType(List<LabelValueDTO> activityByType) {
        this.activityByType = activityByType;
    }
}
