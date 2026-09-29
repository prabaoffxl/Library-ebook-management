package com.library.dto;

public class LicenseSuspendRequest {

    private String reason;

    public LicenseSuspendRequest() {
    }

    public LicenseSuspendRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
