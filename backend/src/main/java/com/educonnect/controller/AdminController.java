package com.educonnect.controller;

import com.educonnect.dto.*;
import com.educonnect.enums.ResourceStatus;
import com.educonnect.security.UserPrincipal;
import com.educonnect.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for administrator dashboard, moderation queue, user management, and audit logs.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * GET /api/admin/dashboard
     * Returns summary metric cards for registered users, materials, and queue counts.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardStatsResponse> getDashboardStats() {
        AdminDashboardStatsResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/admin/resources/pending
     * Retrieves paginated pending resources awaiting moderator verification.
     */
    @GetMapping("/resources/pending")
    public ResponseEntity<ResourcePageResponse> getPendingResources(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        ResourcePageResponse response = adminService.getPendingResources(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/resources
     * Returns all resources for administration.
     * Supports both paginated responses and JSON array for frontend compatibility.
     */
    @GetMapping("/resources")
    public ResponseEntity<?> getAllResources(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        ResourceStatus resourceStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                resourceStatus = ResourceStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (page != null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            ResourcePageResponse pageResponse = adminService.getAllResourcesPaginated(resourceStatus, pageable);
            return ResponseEntity.ok(pageResponse);
        } else {
            List<ResourceResponse> listResponse = adminService.getAllResourcesList(resourceStatus);
            return ResponseEntity.ok(listResponse);
        }
    }

    /**
     * PATCH /api/admin/resources/{id}/approve
     * Approves a resource submission.
     */
    @PatchMapping("/resources/{id}/approve")
    public ResponseEntity<ResourceResponse> approveResource(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal admin
    ) {
        ResourceResponse response = adminService.approveResource(id, admin);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/admin/resources/{id}/reject
     * Rejects a resource submission with optional rejection reason.
     */
    @PatchMapping("/resources/{id}/reject")
    public ResponseEntity<ResourceResponse> rejectResource(
            @PathVariable("id") Long id,
            @Valid @RequestBody(required = false) RejectResourceRequest request,
            @AuthenticationPrincipal UserPrincipal admin
    ) {
        String reason = (request != null) ? request.getReason() : null;
        ResourceResponse response = adminService.rejectResource(id, reason, admin);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/admin/resources/{id}
     * Deletes a resource permanently.
     */
    @DeleteMapping("/resources/{id}")
    public ResponseEntity<Map<String, Object>> deleteResource(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal admin
    ) {
        adminService.deleteResource(id, admin);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Resource removed by administrator");
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/users
     * Retrieves paginated registered students and faculty.
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserResponse> users = adminService.getUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * PATCH /api/admin/users/{id}/status
     * Enables or disables a user account.
     */
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserStatusUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal admin
    ) {
        UserResponse response = adminService.updateUserStatus(id, request.getEnabled(), admin);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/audit-logs
     * Retrieves administrative audit logs.
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AdminAuditLogResponse>> getAuditLogs(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminAuditLogResponse> logs = adminService.getAuditLogs(pageable);
        return ResponseEntity.ok(logs);
    }
}
