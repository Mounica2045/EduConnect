package com.educonnect.dto;

/**
 * DTO indicating whether a resource is bookmarked by the authenticated user.
 */
public class BookmarkStatusResponse {

    private Long resourceId;
    private boolean isBookmarked;

    public BookmarkStatusResponse() {
    }

    public BookmarkStatusResponse(Long resourceId, boolean isBookmarked) {
        this.resourceId = resourceId;
        this.isBookmarked = isBookmarked;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }
}
