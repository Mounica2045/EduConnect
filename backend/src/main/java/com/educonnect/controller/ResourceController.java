package com.educonnect.controller;

import com.educonnect.dto.*;
import com.educonnect.enums.ResourceStatus;
import com.educonnect.security.UserPrincipal;
import com.educonnect.service.ResourceService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Educational Resource Management and Search.
 * Exposes endpoints for resource browsing, multi-criteria filtering, upload,
 * metadata updates, file streaming download, and administrative review.
 */
@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /**
     * GET /api/resources
     * Multi-criteria faceted search with pagination and sorting.
     */
    @GetMapping
    public ResponseEntity<ResourcePageResponse> searchResources(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "semester", required = false) String semester,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "sort", required = false, defaultValue = "recent") String sort,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "6") int size
    ) {
        ResourcePageResponse response = resourceService.searchResources(
                keyword, category, department, semester, subject, fileType, sort, page, size
        );
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/resources/recent
     * Retrieves the latest approved resources (e.g. for the home page feed).
     */
    @GetMapping("/recent")
    public ResponseEntity<List<ResourceResponse>> getRecentResources(
            @RequestParam(value = "limit", required = false, defaultValue = "6") int limit
    ) {
        List<ResourceResponse> recent = resourceService.getRecentResources(limit);
        return ResponseEntity.ok(recent);
    }

    /**
     * GET /api/resources/categories
     * Retrieves all available resource categories and their metadata badges.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(resourceService.getCategories());
    }

    /**
     * GET /api/resources/category/{slug}
     * Browse resources filtered by category slug.
     */
    @GetMapping("/category/{slug}")
    public ResponseEntity<ResourcePageResponse> getByCategory(
            @PathVariable("slug") String slug,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "6") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(resourceService.getResourcesByCategory(slug, pageable));
    }

    /**
     * GET /api/resources/subject/{subject}
     * Browse resources filtered by subject name.
     */
    @GetMapping("/subject/{subject}")
    public ResponseEntity<ResourcePageResponse> getBySubject(
            @PathVariable("subject") String subject,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "6") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(resourceService.getResourcesBySubject(subject, pageable));
    }

    /**
     * GET /api/resources/mine
     * Retrieves resources uploaded by the authenticated user.
     */
    @GetMapping("/mine")
    public ResponseEntity<ResourcePageResponse> getMyResources(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(resourceService.getMyResources(principal, pageable));
    }

    /**
     * GET /api/resources/{id}
     * Retrieves single resource details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getResourceById(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ResourceResponse response = resourceService.getResourceById(id, principal);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/resources (Multipart file upload)
     * Enforces authentication, file validation, and queues for review.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResourceResponse> createResourceMultipart(
            @Valid @ModelAttribute ResourceCreateRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ResourceResponse created = resourceService.createResource(request, file, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * POST /api/resources (JSON payload)
     * For REST API clients submitting link-based resources without file upload.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResourceResponse> createResourceJson(
            @Valid @RequestBody ResourceCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ResourceResponse created = resourceService.createResource(request, null, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/resources/{id}
     * Updates permitted resource metadata.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResourceResponse> updateResource(
            @PathVariable("id") Long id,
            @Valid @RequestBody ResourceUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ResourceResponse updated = resourceService.updateResource(id, request, principal);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/resources/{id}
     * Deletes a resource (owner or administrator).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteResource(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        resourceService.deleteResource(id, principal);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Resource deleted successfully");
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/resources/{id}/download
     * Streams file download and increments verified download count.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadResource(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ResourceService.DownloadDetails download = resourceService.downloadResource(id, principal);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if ("PDF".equalsIgnoreCase(download.getFileType())) {
            mediaType = MediaType.APPLICATION_PDF;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.getFileName() + "\"")
                .body(download.getResource());
    }

    /**
     * PATCH /api/resources/{id}/approve
     * Approves a resource for library publication (Admin only).
     */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceResponse> approveResource(@PathVariable("id") Long id) {
        ResourceResponse approved = resourceService.approveResource(id);
        return ResponseEntity.ok(approved);
    }

    /**
     * PATCH /api/resources/{id}/reject
     * Rejects a resource (Admin only).
     */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceResponse> rejectResource(@PathVariable("id") Long id) {
        ResourceResponse rejected = resourceService.rejectResource(id);
        return ResponseEntity.ok(rejected);
    }
}
