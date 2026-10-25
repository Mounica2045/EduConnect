package com.educonnect.entity;

import com.educonnect.enums.ResourceCategory;
import com.educonnect.enums.ResourceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Shared JPA Resource Entity for EduConnect.
 * Represents notes, question papers, lab manuals, and tutorials.
 */
@Entity
@Table(
    name = "resources",
    indexes = {
        @Index(name = "idx_resources_status_created", columnList = "status, created_at"),
        @Index(name = "idx_resources_category", columnList = "category"),
        @Index(name = "idx_resources_department", columnList = "department"),
        @Index(name = "idx_resources_semester", columnList = "semester"),
        @Index(name = "idx_resources_subject", columnList = "subject"),
        @Index(name = "idx_resources_uploader", columnList = "uploaded_by_id")
    }
)
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Resource title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Subject course name is required")
    @Size(max = 100, message = "Subject name cannot exceed 100 characters")
    @Column(name = "subject", nullable = false, length = 100)
    private String subject;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department cannot exceed 100 characters")
    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Size(max = 50, message = "Semester cannot exceed 50 characters")
    @Column(name = "semester", length = 50)
    private String semester;

    @NotNull(message = "Resource category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ResourceCategory category = ResourceCategory.NOTES;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_location", length = 500)
    private String fileLocation;

    @Column(name = "file_type", length = 50)
    private String fileType = "PDF";

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "external_url", length = 500)
    private String externalUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", foreignKey = @ForeignKey(name = "fk_resources_users"))
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.SET_NULL)
    private User uploadedBy;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ResourceStatus status = ResourceStatus.PENDING;

    @Column(name = "download_count", nullable = false)
    private int downloadCount = 0;

    @Column(name = "tags", length = 255)
    private String tags;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Resource() {
    }

    public Resource(String title, String description, String subject, String department, String semester,
                    ResourceCategory category, User uploadedBy) {
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.department = department;
        this.semester = semester;
        this.category = category != null ? category : ResourceCategory.NOTES;
        this.uploadedBy = uploadedBy;
        this.status = ResourceStatus.PENDING;
        this.downloadCount = 0;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public ResourceCategory getCategory() {
        return category;
    }

    public void setCategory(ResourceCategory category) {
        this.category = category;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public User getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
