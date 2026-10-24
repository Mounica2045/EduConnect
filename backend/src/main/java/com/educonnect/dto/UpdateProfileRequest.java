package com.educonnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating user profile details.
 */
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    @Size(max = 50, message = "Semester cannot exceed 50 characters")
    private String semester;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String fullName, String department, String semester) {
        this.fullName = fullName;
        this.department = department;
        this.semester = semester;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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
