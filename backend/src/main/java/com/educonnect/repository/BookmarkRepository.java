package com.educonnect.repository;

import com.educonnect.entity.Bookmark;
import com.educonnect.enums.ResourceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Bookmark entity.
 */
@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserIdAndResourceId(Long userId, Long resourceId);

    Optional<Bookmark> findByUserIdAndResourceId(Long userId, Long resourceId);

    void deleteByUserIdAndResourceId(Long userId, Long resourceId);

    /**
     * Retrieves paginated bookmarks for a user, filtering for approved resources to prevent showing draft/rejected items.
     */
    @Query("SELECT b FROM Bookmark b JOIN FETCH b.resource r WHERE b.user.id = :userId AND r.status = :status")
    Page<Bookmark> findByUserIdAndResourceStatus(
            @Param("userId") Long userId,
            @Param("status") ResourceStatus status,
            Pageable pageable
    );

    /**
     * Unpaginated list for frontend array serialization fallback.
     */
    @Query("SELECT b FROM Bookmark b JOIN FETCH b.resource r WHERE b.user.id = :userId AND r.status = :status ORDER BY b.createdAt DESC")
    List<Bookmark> findByUserIdAndResourceStatusOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("status") ResourceStatus status
    );

    long countByUserId(Long userId);
}
