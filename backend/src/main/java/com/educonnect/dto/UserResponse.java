package com.educonnect.dto;

import com.educonnect.entity.User;
import com.educonnect.enums.Role;

/**
 * Safe DTO for exposing user details to client.
 * Guarantees passwords, hashes, and internal security details are never exposed.
 */
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String department;
    private String semester;
    private Role role;
    private boolean enabled;

    public UserResponse() {
    }

    public UserResponse(Long id, String fullName, String email, String department, String semester, Role role, boolean enabled) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.semester = semester;
        this.role = role;
        this.enabled = enabled;
    }

    public static UserResponse from(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getDepartment(),
                user.getSemester(),
                user.getRole(),
                user.isEnabled()
        );
    }

    public static UserResponse fromEntity(User user) {
        return from(user);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
