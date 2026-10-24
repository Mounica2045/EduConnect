package com.educonnect.enums;

/**
 * EduConnect User Roles for Role-Based Access Control (RBAC).
 * Supports Spring Security authority resolution.
 */
public enum Role {
    STUDENT,
    ADMIN;

    /**
     * Helper to return standard Spring Security authority prefix.
     */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
