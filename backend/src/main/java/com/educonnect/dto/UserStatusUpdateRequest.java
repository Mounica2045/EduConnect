package com.educonnect.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload for enabling or disabling a user account.
 */
public class UserStatusUpdateRequest {

    @NotNull(message = "Account enabled status is required")
    private Boolean enabled;

    public UserStatusUpdateRequest() {
    }

    public UserStatusUpdateRequest(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
