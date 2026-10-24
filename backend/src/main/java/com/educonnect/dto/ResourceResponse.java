package com.educonnect.dto;

import com.educonnect.entity.Resource;
import com.educonnect.enums.ResourceCategory;

import java.time.Instant;

/**
 * Educational Resource API Response DTO.
 * Exposes sanitized metadata seamlessly compatible with Member 1's frontend.
 */
public class ResourceResponse {

    private Long id;
    private String title;
    private String description;
    private String subject;
    private String department;
    private String semester;
    private String category;
    private String categoryLabel;
    private String badgeClass;
    private String fileName;
    private String fileType;
    private String fileSize;
    private Long fileSizeBytes;
    private String externalUrl;
    private String tags;
    private int downloadCount;
    private String status;
    private boolean isApproved;
    private String contributorName;
    private ResourceUploaderDto uploadedBy;
    private String uploadDate;
    private Instant createdAt;
    private Instant updatedAt;

    public ResourceResponse() {
    }

    public static ResourceResponse fromEntity(Resource entity) {
        if (entity == null) {
            return null;
        }

        ResourceResponse res = new ResourceResponse();
        res.setId(entity.getId());
        res.setTitle(entity.getTitle());
        res.setDescription(entity.getDescription());
        res.setSubject(entity.getSubject());
        res.setDepartment(entity.getDepartment());
        res.setSemester(entity.getSemester());

        ResourceCategory cat = entity.getCategory() != null ? entity.getCategory() : ResourceCategory.NOTES;
        res.setCategory(cat.getSlug());
        res.setCategoryLabel(cat.getLabel());
        res.setBadgeClass(cat.getBadgeClass());

        res.setFileName(entity.getFileName());
        res.setFileType(entity.getFileType() != null ? entity.getFileType().toUpperCase() : "PDF");
        res.setFileSizeBytes(entity.getFileSize());
        res.setFileSize(formatFileSize(entity.getFileSize()));

        res.setExternalUrl(entity.getExternalUrl());
        res.setTags(entity.getTags());
        res.setDownloadCount(entity.getDownloadCount());
        res.setStatus(entity.getStatus() != null ? entity.getStatus().name() : "PENDING");
        res.setApproved(entity.getStatus() == com.educonnect.enums.ResourceStatus.APPROVED);

        if (entity.getUploadedBy() != null) {
            res.setUploadedBy(ResourceUploaderDto.fromEntity(entity.getUploadedBy()));
            res.setContributorName(entity.getUploadedBy().getFullName());
        } else {
            res.setContributorName("Verified Contributor");
        }

        res.setCreatedAt(entity.getCreatedAt());
        res.setUpdatedAt(entity.getUpdatedAt());
        if (entity.getCreatedAt() != null) {
            res.setUploadDate(entity.getCreatedAt().toString());
        }

        return res;
    }

    private static String formatFileSize(Long bytes) {
        if (bytes == null || bytes <= 0) {
            return "0 KB";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String[] units = {"B", "KB", "MB", "GB"};
        if (exp >= units.length) {
            exp = units.length - 1;
        }
        return String.format("%.1f %s", bytes / Math.pow(1024, exp), units[exp]);
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryLabel() {
        return categoryLabel;
    }

    public void setCategoryLabel(String categoryLabel) {
        this.categoryLabel = categoryLabel;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public void setBadgeClass(String badgeClass) {
        this.badgeClass = badgeClass;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
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

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public String getContributorName() {
        return contributorName;
    }

    public void setContributorName(String contributorName) {
        this.contributorName = contributorName;
    }

    public ResourceUploaderDto getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(ResourceUploaderDto uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
