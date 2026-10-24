package com.educonnect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for student registration requests.
 * Explicitly excludes role to prevent unauthorized privilege escalation.
 */
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Please provide a valid email address format")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters long")
    private String password;

    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    @Size(max = 50, message = "Semester cannot exceed 50 characters")
    private String semester;

    public RegisterRequest() {
    }

    public RegisterRequest(String fullName, String email, String password, String department, String semester) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.department = department;
        this.semester = semester;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email != null ? email.trim().toLowerCase() : null;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
