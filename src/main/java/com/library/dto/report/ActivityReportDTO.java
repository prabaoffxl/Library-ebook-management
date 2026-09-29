package com.library.dto.report;

import java.util.ArrayList;
import java.util.List;

public class ActivityReportDTO {

    private long totalActivities;
    private List<LabelValueDTO> activityByType = new ArrayList<>();
    private List<LabelValueDTO> activityByDay = new ArrayList<>();
    private List<LabelValueDTO> activityByMonth = new ArrayList<>();
    private List<LabelValueDTO> mostActiveUsers = new ArrayList<>();
    private List<LabelValueDTO> mostViewedBooks = new ArrayList<>();
    private List<LabelValueDTO> mostDownloadedBooks = new ArrayList<>();

    public ActivityReportDTO() {
    }

    public long getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(long totalActivities) {
        this.totalActivities = totalActivities;
    }

    public List<LabelValueDTO> getActivityByType() {
        return activityByType;
    }

    public void setActivityByType(List<LabelValueDTO> activityByType) {
        this.activityByType = activityByType;
    }

    public List<LabelValueDTO> getActivityByDay() {
        return activityByDay;
    }

    public void setActivityByDay(List<LabelValueDTO> activityByDay) {
        this.activityByDay = activityByDay;
    }

    public List<LabelValueDTO> getActivityByMonth() {
        return activityByMonth;
    }

    public void setActivityByMonth(List<LabelValueDTO> activityByMonth) {
        this.activityByMonth = activityByMonth;
    }

    public List<LabelValueDTO> getMostActiveUsers() {
        return mostActiveUsers;
    }

    public void setMostActiveUsers(List<LabelValueDTO> mostActiveUsers) {
        this.mostActiveUsers = mostActiveUsers;
    }

    public List<LabelValueDTO> getMostViewedBooks() {
        return mostViewedBooks;
    }

    public void setMostViewedBooks(List<LabelValueDTO> mostViewedBooks) {
        this.mostViewedBooks = mostViewedBooks;
    }

    public List<LabelValueDTO> getMostDownloadedBooks() {
        return mostDownloadedBooks;
    }

    public void setMostDownloadedBooks(List<LabelValueDTO> mostDownloadedBooks) {
        this.mostDownloadedBooks = mostDownloadedBooks;
    }
}
