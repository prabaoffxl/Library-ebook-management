package com.library.dto;

import com.library.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public class UserStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;

    public UserStatusUpdateRequest() {
    }

    public UserStatusUpdateRequest(UserStatus status) {
        this.status = status;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
