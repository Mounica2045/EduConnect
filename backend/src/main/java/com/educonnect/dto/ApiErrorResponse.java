package com.educonnect.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized API Error Response.
 * Guarantees that internal stack traces and database implementation details are never leaked.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private int status;
    private String message;
    private Map<String, String> errors;
    private Instant timestamp;
    private String path;

    public ApiErrorResponse() {
        this.timestamp = Instant.now();
    }

    public ApiErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public ApiErrorResponse(int status, String message, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors;
        this.timestamp = Instant.now();
    }

    public ApiErrorResponse(int status, String message, Map<String, String> errors, String path) {
        this.status = status;
        this.message = message;
        this.errors = errors;
        this.timestamp = Instant.now();
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
