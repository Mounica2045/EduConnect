package com.educonnect.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO for updating educational resource metadata.
 */
public class ResourceUpdateRequest {

    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(min = 10, max = 3000, message = "Description must be at least 10 characters")
    private String description;

    @Size(max = 100, message = "Subject name cannot exceed 100 characters")
    private String subject;

    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    @Size(max = 50, message = "Semester cannot exceed 50 characters")
    private String semester;

    private String category;

    @Size(max = 500, message = "External URL cannot exceed 500 characters")
    private String externalUrl;

    @Size(max = 255, message = "Tags cannot exceed 255 characters")
    private String tags;

    public ResourceUpdateRequest() {
    }

    public ResourceUpdateRequest(String title, String description, String subject, String department, String semester, String category, String externalUrl, String tags) {
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.department = department;
        this.semester = semester;
        this.category = category;
        this.externalUrl = externalUrl;
        this.tags = tags;
    }

    // Getters and Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
