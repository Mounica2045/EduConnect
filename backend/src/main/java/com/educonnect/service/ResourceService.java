package com.educonnect.service;

import com.educonnect.dto.*;
import com.educonnect.entity.Resource;
import com.educonnect.entity.User;
import com.educonnect.enums.ResourceCategory;
import com.educonnect.enums.ResourceStatus;
import com.educonnect.enums.Role;
import com.educonnect.exception.FileStorageException;
import com.educonnect.exception.ResourceNotFoundException;
import com.educonnect.exception.UnauthorizedResourceAccessException;
import com.educonnect.repository.ResourceRepository;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.UserPrincipal;
import com.educonnect.specification.ResourceSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core business service for managing educational resources, faceted search,
 * access control, file storage integration, and moderation workflows.
 */
@Service
public class ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public ResourceService(
            ResourceRepository resourceRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService
    ) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Faceted search and filtering for approved educational materials.
     */
    @Transactional(readOnly = true)
    public ResourcePageResponse searchResources(
            String keyword,
            String category,
            String department,
            String semester,
            String subject,
            String fileType,
            String sortBy,
            int page,
            int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = (size <= 0 || size > 100) ? 6 : size;

        Sort sort = resolveSort(sortBy);
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        Specification<Resource> spec = ResourceSpecification.filter(
                keyword, category, department, semester, subject, fileType, ResourceStatus.APPROVED
        );

        Page<Resource> resultPage = resourceRepository.findAll(spec, pageable);
        return ResourcePageResponse.fromPage(resultPage);
    }

    /**
     * Retrieves single resource by ID with access control validation.
     * Public users can only view APPROVED resources.
     * The uploader and administrators can view PENDING and REJECTED resources.
     */
    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id, UserPrincipal principal) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        if (resource.getStatus() != ResourceStatus.APPROVED) {
            boolean hasPermission = false;
            if (principal != null) {
                boolean isAdmin = principal.getRole() == Role.ADMIN;
                boolean isOwner = resource.getUploadedBy() != null && resource.getUploadedBy().getId().equals(principal.getId());
                hasPermission = isAdmin || isOwner;
            }

            if (!hasPermission) {
                throw new ResourceNotFoundException("Resource not found or pending administrator verification.");
            }
        }

        return ResourceResponse.fromEntity(resource);
    }

    /**
     * Retrieves recent approved resources (e.g. for homepage feed).
     */
    @Transactional(readOnly = true)
    public List<ResourceResponse> getRecentResources(int limit) {
        int safeLimit = limit <= 0 ? 6 : Math.min(limit, 24);
        Pageable pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Resource> page = resourceRepository.findAll(
                ResourceSpecification.filter(null, null, null, null, null, null, ResourceStatus.APPROVED),
                pageable
        );
        return page.getContent().stream()
                .map(ResourceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves resources by category slug with pagination.
     */
    @Transactional(readOnly = true)
    public ResourcePageResponse getResourcesByCategory(String categorySlug, Pageable pageable) {
        Specification<Resource> spec = ResourceSpecification.filter(
                null, categorySlug, null, null, null, null, ResourceStatus.APPROVED
        );
        Page<Resource> page = resourceRepository.findAll(spec, pageable);
        return ResourcePageResponse.fromPage(page);
    }

    /**
     * Retrieves resources by subject course name with pagination.
     */
    @Transactional(readOnly = true)
    public ResourcePageResponse getResourcesBySubject(String subject, Pageable pageable) {
        Specification<Resource> spec = ResourceSpecification.filter(
                null, null, null, null, subject, null, ResourceStatus.APPROVED
        );
        Page<Resource> page = resourceRepository.findAll(spec, pageable);
        return ResourcePageResponse.fromPage(page);
    }

    /**
     * Retrieves resources uploaded by the authenticated student.
     */
    @Transactional(readOnly = true)
    public ResourcePageResponse getMyResources(UserPrincipal principal, Pageable pageable) {
        if (principal == null) {
            throw new UnauthorizedResourceAccessException("Authentication required to view your uploads.");
        }
        Page<Resource> page = resourceRepository.findByUploadedById(principal.getId(), pageable);
        return ResourcePageResponse.fromPage(page);
    }

    /**
     * Creates and stores a new educational resource.
     * Enforces file validation, sanitized filenames, and moderator approval workflow.
     */
    @Transactional
    public ResourceResponse createResource(
            ResourceCreateRequest request,
            MultipartFile file,
            UserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedResourceAccessException("Authentication required to submit resources.");
        }

        User uploader = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Uploader account not found with ID: " + principal.getId()));

        // Validate that either a document file or external URL is provided
        boolean hasFile = file != null && !file.isEmpty();
        boolean hasUrl = StringUtils.hasText(request.getExternalUrl());

        if (!hasFile && !hasUrl) {
            throw new FileStorageException("Please upload a document file (.pdf, .docx, .pptx) or specify an external resource link.");
        }

        ResourceCategory category = ResourceCategory.fromString(request.getCategory());

        Resource resource = new Resource();
        resource.setTitle(request.getTitle().trim());
        resource.setDescription(request.getDescription().trim());
        resource.setSubject(request.getSubject().trim());
        resource.setDepartment(request.getDepartment().trim());
        resource.setSemester(request.getSemester() != null ? request.getSemester().trim() : null);
        resource.setCategory(category);
        resource.setTags(request.getTags() != null ? request.getTags().trim() : null);
        resource.setExternalUrl(request.getExternalUrl() != null ? request.getExternalUrl().trim() : null);
        resource.setUploadedBy(uploader);
        resource.setDownloadCount(0);

        // Administrator uploads are automatically approved; student uploads enter the pending queue
        if (principal.getRole() == Role.ADMIN) {
            resource.setStatus(ResourceStatus.APPROVED);
        } else {
            resource.setStatus(ResourceStatus.PENDING);
        }

        // Store file if attached
        if (hasFile) {
            FileStorageService.StoredFile storedFile = fileStorageService.storeFile(file);
            resource.setFileName(storedFile.getOriginalFilename());
            resource.setFileLocation(storedFile.getFileLocation());
            resource.setFileType(storedFile.getFileType());
            resource.setFileSize(storedFile.getFileSize());
        } else {
            resource.setFileName("External Link");
            resource.setFileType("URL");
            resource.setFileSize(0L);
        }

        Resource saved = resourceRepository.save(resource);
        logger.info("Created resource id={}, title='{}', status={}, uploader='{}'",
                saved.getId(), saved.getTitle(), saved.getStatus(), uploader.getEmail());

        return ResourceResponse.fromEntity(saved);
    }

    /**
     * Updates permitted resource metadata.
     * Enforces ownership validation: only the original uploader or an admin can modify.
     */
    @Transactional
    public ResourceResponse updateResource(
            Long id,
            ResourceUpdateRequest request,
            UserPrincipal principal
    ) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        checkCanModify(resource, principal, "modify");

        if (StringUtils.hasText(request.getTitle())) {
            resource.setTitle(request.getTitle().trim());
        }
        if (StringUtils.hasText(request.getDescription())) {
            resource.setDescription(request.getDescription().trim());
        }
        if (StringUtils.hasText(request.getSubject())) {
            resource.setSubject(request.getSubject().trim());
        }
        if (StringUtils.hasText(request.getDepartment())) {
            resource.setDepartment(request.getDepartment().trim());
        }
        if (request.getSemester() != null) {
            resource.setSemester(request.getSemester().trim());
        }
        if (StringUtils.hasText(request.getCategory())) {
            resource.setCategory(ResourceCategory.fromString(request.getCategory()));
        }
        if (request.getTags() != null) {
            resource.setTags(request.getTags().trim());
        }
        if (request.getExternalUrl() != null) {
            resource.setExternalUrl(request.getExternalUrl().trim());
        }

        Resource updated = resourceRepository.save(resource);
        logger.info("Updated resource id={}, title='{}' by user={}", updated.getId(), updated.getTitle(), principal.getEmail());
        return ResourceResponse.fromEntity(updated);
    }

    /**
     * Deletes a resource and removes associated physical file.
     * Enforces ownership validation: only the original uploader or an admin can delete.
     */
    @Transactional
    public void deleteResource(Long id, UserPrincipal principal) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        checkCanModify(resource, principal, "delete");

        if (resource.getFileLocation() != null) {
            fileStorageService.deleteFile(resource.getFileLocation());
        }

        resourceRepository.delete(resource);
        logger.info("Deleted resource id={} by user={}", id, principal.getEmail());
    }

    /**
     * Approves a submitted resource for public publication (Administrator function).
     */
    @Transactional
    public ResourceResponse approveResource(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        resource.setStatus(ResourceStatus.APPROVED);
        Resource saved = resourceRepository.save(resource);
        logger.info("Resource id={} marked APPROVED by administrator", id);
        return ResourceResponse.fromEntity(saved);
    }

    /**
     * Rejects a submitted resource (Administrator function).
     */
    @Transactional
    public ResourceResponse rejectResource(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        resource.setStatus(ResourceStatus.REJECTED);
        Resource saved = resourceRepository.save(resource);
        logger.info("Resource id={} marked REJECTED by administrator", id);
        return ResourceResponse.fromEntity(saved);
    }

    /**
     * Admin view of all resources filtered by optional moderation status.
     */
    @Transactional(readOnly = true)
    public ResourcePageResponse getAllResourcesForAdmin(ResourceStatus status, Pageable pageable) {
        Specification<Resource> spec = ResourceSpecification.filter(null, null, null, null, null, null, status);
        Page<Resource> page = resourceRepository.findAll(spec, pageable);
        return ResourcePageResponse.fromPage(page);
    }

    /**
     * Downloads file stream and increments verified download count.
     */
    @Transactional
    public DownloadDetails downloadResource(Long id, UserPrincipal principal) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        // Validate access
        if (resource.getStatus() != ResourceStatus.APPROVED) {
            boolean hasPermission = false;
            if (principal != null) {
                boolean isAdmin = principal.getRole() == Role.ADMIN;
                boolean isOwner = resource.getUploadedBy() != null && resource.getUploadedBy().getId().equals(principal.getId());
                hasPermission = isAdmin || isOwner;
            }

            if (!hasPermission) {
                throw new ResourceNotFoundException("Resource file not accessible or pending moderator review.");
            }
        }

        if (resource.getFileLocation() == null) {
            throw new FileStorageException("No downloadable document is attached to this resource.");
        }

        org.springframework.core.io.Resource springResource = fileStorageService.loadFileAsResource(resource.getFileLocation());

        // Increment download view count
        resource.incrementDownloadCount();
        resourceRepository.save(resource);

        String filename = resource.getFileName() != null ? resource.getFileName() : "resource_" + id + ".pdf";
        return new DownloadDetails(springResource, filename, resource.getFileType(), resource.getFileSize());
    }

    /**
     * Returns all categories with labels, badges, and slugs.
     */
    public List<CategoryResponse> getCategories() {
        return Arrays.stream(ResourceCategory.values())
                .map(CategoryResponse::fromCategory)
                .collect(Collectors.toList());
    }

    /**
     * Helper to verify ownership or admin privileges before modifying or deleting.
     */
    private void checkCanModify(Resource resource, UserPrincipal principal, String action) {
        if (principal == null) {
            throw new UnauthorizedResourceAccessException("Authentication required to " + action + " this resource.");
        }

        boolean isAdmin = principal.getRole() == Role.ADMIN;
        boolean isOwner = resource.getUploadedBy() != null && resource.getUploadedBy().getId().equals(principal.getId());

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedResourceAccessException("You are not authorized to " + action + " another student's resource.");
        }
    }

    /**
     * Translates frontend sort parameter to Spring Data Sort specification.
     */
    private Sort resolveSort(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sortBy.toLowerCase()) {
            case "popular" -> Sort.by(Sort.Direction.DESC, "downloadCount");
            case "title_asc" -> Sort.by(Sort.Direction.ASC, "title");
            case "title_desc" -> Sort.by(Sort.Direction.DESC, "title");
            case "recent" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    /**
     * Download metadata container.
     */
    public static class DownloadDetails {
        private final org.springframework.core.io.Resource resource;
        private final String fileName;
        private final String fileType;
        private final Long fileSize;

        public DownloadDetails(org.springframework.core.io.Resource resource, String fileName, String fileType, Long fileSize) {
            this.resource = resource;
            this.fileName = fileName;
            this.fileType = fileType;
            this.fileSize = fileSize;
        }

        public org.springframework.core.io.Resource getResource() {
            return resource;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFileType() {
            return fileType;
        }

        public Long getFileSize() {
            return fileSize;
        }
    }
}
