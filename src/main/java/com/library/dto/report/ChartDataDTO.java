package com.library.dto.report;

import java.util.ArrayList;
import java.util.List;

public class ChartDataDTO {
    private List<String> labels = new ArrayList<>();
    private List<Number> values = new ArrayList<>();

    public ChartDataDTO() {
    }

    public ChartDataDTO(List<String> labels, List<Number> values) {
        this.labels = labels != null ? labels : new ArrayList<>();
        this.values = values != null ? values : new ArrayList<>();
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Number> getValues() {
        return values;
    }

    public void setValues(List<Number> values) {
        this.values = values;
    }
}
