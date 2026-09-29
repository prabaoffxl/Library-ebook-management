package com.library.dto.report;

public class LabelValueDTO {
    private String label;
    private Number value;

    public LabelValueDTO() {
    }

    public LabelValueDTO(String label, Number value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Number getValue() {
        return value;
    }

    public void setValue(Number value) {
        this.value = value;
    }
}
