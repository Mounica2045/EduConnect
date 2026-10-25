package com.educonnect.dto;

import com.educonnect.entity.AdminAuditLog;
import java.time.Instant;

/**
 * Representation of an administrative action audit entry.
 */
public class AdminAuditLogResponse {

    private Long id;
    private Long adminId;
    private String adminEmail;
    private String action;
    private String targetType;
    private Long targetId;
    private String details;
    private Instant createdAt;

    public AdminAuditLogResponse() {
    }

    public AdminAuditLogResponse(Long id, Long adminId, String adminEmail, String action, String targetType, Long targetId, String details, Instant createdAt) {
        this.id = id;
        this.adminId = adminId;
        this.adminEmail = adminEmail;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.details = details;
        this.createdAt = createdAt;
    }

    public static AdminAuditLogResponse fromEntity(AdminAuditLog log) {
        if (log == null) {
            return null;
        }
        return new AdminAuditLogResponse(
                log.getId(),
                log.getAdminId(),
                log.getAdminEmail(),
                log.getAction(),
                log.getTargetType(),
                log.getTargetId(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
