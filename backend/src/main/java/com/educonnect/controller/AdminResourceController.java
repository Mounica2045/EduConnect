package com.educonnect.controller;

import com.educonnect.dto.ResourcePageResponse;
import com.educonnect.dto.ResourceResponse;
import com.educonnect.enums.ResourceStatus;
import com.educonnect.security.UserPrincipal;
import com.educonnect.service.ResourceService;
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
 * Controller bridging administrator moderation endpoints expected by Member 1's admin dashboard.
 */
@RestController
@RequestMapping("/api/admin/resources")
@PreAuthorize("hasRole('ADMIN')")
public class AdminResourceController {

    private final ResourceService resourceService;

    public AdminResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /**
     * GET /api/admin/resources
     * Returns all resources for moderation queue and library management.
     */
    @GetMapping
    public ResponseEntity<List<ResourceResponse>> getAllResourcesForAdmin(
            @RequestParam(value = "status", required = false) String status
    ) {
        ResourceStatus resourceStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                resourceStatus = ResourceStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Pageable pageable = PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "createdAt"));
        ResourcePageResponse page = resourceService.getAllResourcesForAdmin(resourceStatus, pageable);
        return ResponseEntity.ok(page.getContent());
    }

    /**
     * PATCH /api/admin/resources/{id}/approve
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ResourceResponse> approveResource(@PathVariable("id") Long id) {
        ResourceResponse approved = resourceService.approveResource(id);
        return ResponseEntity.ok(approved);
    }

    /**
     * PATCH /api/admin/resources/{id}/reject
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ResourceResponse> rejectResource(@PathVariable("id") Long id) {
        ResourceResponse rejected = resourceService.rejectResource(id);
        return ResponseEntity.ok(rejected);
    }

    /**
     * DELETE /api/admin/resources/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteResource(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        resourceService.deleteResource(id, principal);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Resource removed by administrator");
        response.put("id", id);
        return ResponseEntity.ok(response);
    }
}
