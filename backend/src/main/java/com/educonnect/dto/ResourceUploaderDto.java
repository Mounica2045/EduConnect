package com.educonnect.dto;

import com.educonnect.entity.User;

/**
 * Safe representation of a user who uploaded a resource.
 * Strips all sensitive credentials like password hashes and internal tokens.
 */
public class ResourceUploaderDto {

    private Long id;
    private String fullName;
    private String email;
    private String department;
    private String semester;

    public ResourceUploaderDto() {
    }

    public ResourceUploaderDto(Long id, String fullName, String email, String department, String semester) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.semester = semester;
    }

    public static ResourceUploaderDto fromEntity(User user) {
        if (user == null) {
            return new ResourceUploaderDto(null, "Anonymous Student", null, null, null);
        }
        return new ResourceUploaderDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getDepartment(),
                user.getSemester()
        );
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
}
