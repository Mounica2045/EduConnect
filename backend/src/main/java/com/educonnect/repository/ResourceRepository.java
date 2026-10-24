package com.educonnect.repository;

import com.educonnect.entity.Resource;
import com.educonnect.enums.ResourceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for Resource entity with dynamic Specification execution.
 */
@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long>, JpaSpecificationExecutor<Resource> {

    /**
     * Finds the latest approved resources ordered by creation date descending.
     */
    List<Resource> findTop6ByStatusOrderByCreatedAtDesc(ResourceStatus status);

    /**
     * Finds the latest approved resources with configurable limit.
     */
    List<Resource> findTop12ByStatusOrderByCreatedAtDesc(ResourceStatus status);

    /**
     * Retrieves resources uploaded by a specific user with pagination.
     */
    Page<Resource> findByUploadedById(Long userId, Pageable pageable);

    /**
     * Counts resources by workflow status.
     */
    long countByStatus(ResourceStatus status);
}
