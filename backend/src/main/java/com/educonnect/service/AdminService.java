package com.educonnect.service;

import com.educonnect.dto.*;
import com.educonnect.entity.AdminAuditLog;
import com.educonnect.entity.Resource;
import com.educonnect.entity.User;
import com.educonnect.enums.ResourceStatus;
import com.educonnect.exception.ResourceNotFoundException;
import com.educonnect.repository.AdminAuditLogRepository;
import com.educonnect.repository.BookmarkRepository;
import com.educonnect.repository.ResourceRepository;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.UserPrincipal;
import com.educonnect.specification.ResourceSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing administrator operations, dashboard aggregations,
 * content moderation, user status controls, and audit trails.
 */
@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final BookmarkRepository bookmarkRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final FileStorageService fileStorageService;

    public AdminService(
            UserRepository userRepository,
            ResourceRepository resourceRepository,
            BookmarkRepository bookmarkRepository,
            AdminAuditLogRepository auditLogRepository,
            FileStorageService fileStorageService
    ) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.auditLogRepository = auditLogRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Aggregates summary statistics for the administrator dashboard.
     */
    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalResources = resourceRepository.count();
        long approvedResources = resourceRepository.countByStatus(ResourceStatus.APPROVED);
        long pendingResources = resourceRepository.countByStatus(ResourceStatus.PENDING);
        long rejectedResources = resourceRepository.countByStatus(ResourceStatus.REJECTED);
        long totalBookmarks = bookmarkRepository.count();

        return new AdminDashboardStatsResponse(
                totalUsers,
                totalResources,
                approvedResources,
                pendingResources,
                rejectedResources,
                totalBookmarks
        );
    }

    /**
     * Retrieves paginated pending resources awaiting moderator review.
     */
    @Transactional(readOnly = true)
    public ResourcePageResponse getPendingResources(Pageable pageable) {
        Page<Resource> page = resourceRepository.findAll(
                ResourceSpecification.filter(null, null, null, null, null, null, ResourceStatus.PENDING),
                pageable
        );
        return ResourcePageResponse.fromPage(page);
    }

    /**
     * Retrieves paginated resources with optional status filter.
     */
    @Transactional(readOnly = true)
    public ResourcePageResponse getAllResourcesPaginated(ResourceStatus status, Pageable pageable) {
        Page<Resource> page = resourceRepository.findAll(
                ResourceSpecification.filter(null, null, null, null, null, null, status),
                pageable
        );
        return ResourcePageResponse.fromPage(page);
    }

    /**
     * Retrieves full list of resources for frontend admin dashboard table.
     */
    @Transactional(readOnly = true)
    public List<ResourceResponse> getAllResourcesList(ResourceStatus status) {
        List<Resource> list = resourceRepository.findAll(
                ResourceSpecification.filter(null, null, null, null, null, null, status)
        );
        return list.stream()
                .map(ResourceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Approves a resource submission and records the action in the audit log.
     */
    @Transactional
    public ResourceResponse approveResource(Long resourceId, UserPrincipal admin) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(resourceId));

        resource.setStatus(ResourceStatus.APPROVED);
        Resource saved = resourceRepository.save(resource);

        logAction(admin, "APPROVE_RESOURCE", "RESOURCE", resourceId, "Approved title: " + resource.getTitle());
        logger.info("Admin {} approved resource id={}", admin.getEmail(), resourceId);

        return ResourceResponse.fromEntity(saved);
    }

    /**
     * Rejects a resource submission with an optional reason and records it in the audit log.
     */
    @Transactional
    public ResourceResponse rejectResource(Long resourceId, String reason, UserPrincipal admin) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(resourceId));

        resource.setStatus(ResourceStatus.REJECTED);
        Resource saved = resourceRepository.save(resource);

        String details = "Rejected title: " + resource.getTitle() + (StringUtils.hasText(reason) ? " | Reason: " + reason.trim() : "");
        logAction(admin, "REJECT_RESOURCE", "RESOURCE", resourceId, details);
        logger.info("Admin {} rejected resource id={}", admin.getEmail(), resourceId);

        return ResourceResponse.fromEntity(saved);
    }

    /**
     * Permanently deletes a resource and cleans up its stored file.
     */
    @Transactional
    public void deleteResource(Long resourceId, UserPrincipal admin) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(resourceId));

        if (resource.getFileLocation() != null) {
            fileStorageService.deleteFile(resource.getFileLocation());
        }

        resourceRepository.delete(resource);

        logAction(admin, "DELETE_RESOURCE", "RESOURCE", resourceId, "Deleted title: " + resource.getTitle());
        logger.info("Admin {} deleted resource id={}", admin.getEmail(), resourceId);
    }

    /**
     * Retrieves paginated registered students and administrators.
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponse::fromEntity);
    }

    /**
     * Enables or disables a user account with self-lockout protection.
     */
    @Transactional
    public UserResponse updateUserStatus(Long userId, boolean enabled, UserPrincipal admin) {
        if (admin.getId().equals(userId) && !enabled) {
            throw new IllegalArgumentException("Administrators cannot disable their own account.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setEnabled(enabled);
        User saved = userRepository.save(user);

        logAction(admin, "UPDATE_USER_STATUS", "USER", userId, "Set enabled=" + enabled + " for user: " + user.getEmail());
        logger.info("Admin {} updated account status for user id={} to enabled={}", admin.getEmail(), userId, enabled);

        return UserResponse.fromEntity(saved);
    }

    /**
     * Retrieves administrative audit logs.
     */
    @Transactional(readOnly = true)
    public Page<AdminAuditLogResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(AdminAuditLogResponse::fromEntity);
    }

    private void logAction(UserPrincipal admin, String action, String targetType, Long targetId, String details) {
        try {
            AdminAuditLog log = new AdminAuditLog(
                    admin.getId(),
                    admin.getEmail(),
                    action,
                    targetType,
                    targetId,
                    details
            );
            auditLogRepository.save(log);
        } catch (Exception e) {
            logger.error("Failed to write admin audit log: {}", e.getMessage());
        }
    }
}
