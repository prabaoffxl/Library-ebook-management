package com.library.dto.report;

import java.time.LocalDate;

public class DateRangeFilterDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private String quickFilter;

    public DateRangeFilterDTO() {
    }

    public DateRangeFilterDTO(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getQuickFilter() {
        return quickFilter;
    }

    public void setQuickFilter(String quickFilter) {
        this.quickFilter = quickFilter;
    }
}
