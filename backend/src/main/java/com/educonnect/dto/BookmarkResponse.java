package com.educonnect.dto;

import com.educonnect.entity.Bookmark;
import java.time.Instant;

/**
 * DTO representing a bookmarked resource entry.
 */
public class BookmarkResponse {

    private Long id;
    private Long resourceId;
    private Instant createdAt;
    private ResourceResponse resource;

    public BookmarkResponse() {
    }

    public BookmarkResponse(Long id, Long resourceId, Instant createdAt, ResourceResponse resource) {
        this.id = id;
        this.resourceId = resourceId;
        this.createdAt = createdAt;
        this.resource = resource;
    }

    public static BookmarkResponse fromEntity(Bookmark bookmark) {
        if (bookmark == null) {
            return null;
        }
        return new BookmarkResponse(
                bookmark.getId(),
                bookmark.getResource().getId(),
                bookmark.getCreatedAt(),
                ResourceResponse.fromEntity(bookmark.getResource())
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public ResourceResponse getResource() {
        return resource;
    }

    public void setResource(ResourceResponse resource) {
        this.resource = resource;
    }
}
