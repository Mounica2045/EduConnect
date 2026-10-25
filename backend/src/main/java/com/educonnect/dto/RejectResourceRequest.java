package com.educonnect.dto;

import jakarta.validation.constraints.Size;

/**
 * Optional request payload for administrator resource rejection reason.
 */
public class RejectResourceRequest {

    @Size(max = 500, message = "Rejection reason cannot exceed 500 characters")
    private String reason;

    public RejectResourceRequest() {
    }

    public RejectResourceRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
