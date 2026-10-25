package com.educonnect.controller;

import com.educonnect.dto.BookmarkResponse;
import com.educonnect.dto.BookmarkStatusResponse;
import com.educonnect.dto.ResourcePageResponse;
import com.educonnect.dto.ResourceResponse;
import com.educonnect.security.UserPrincipal;
import com.educonnect.service.BookmarkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for student bookmark management.
 */
@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    /**
     * POST /api/bookmarks/{resourceId}
     * Adds an approved resource to the student's saved bookmarks.
     */
    @PostMapping("/{resourceId}")
    public ResponseEntity<BookmarkResponse> addBookmark(
            @PathVariable("resourceId") Long resourceId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        BookmarkResponse response = bookmarkService.addBookmark(resourceId, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * DELETE /api/bookmarks/{resourceId}
     * Removes a resource from the student's saved bookmarks.
     */
    @DeleteMapping("/{resourceId}")
    public ResponseEntity<Map<String, Object>> removeBookmark(
            @PathVariable("resourceId") Long resourceId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        bookmarkService.removeBookmark(resourceId, principal);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Bookmark removed successfully");
        response.put("resourceId", resourceId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/bookmarks/{resourceId}/status
     * Returns whether the specified resource is currently bookmarked by the user.
     */
    @GetMapping("/{resourceId}/status")
    public ResponseEntity<BookmarkStatusResponse> checkBookmarkStatus(
            @PathVariable("resourceId") Long resourceId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        BookmarkStatusResponse response = bookmarkService.checkBookmarkStatus(resourceId, principal);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/bookmarks
     * Returns the student's bookmarked resources.
     * When pagination parameters ('page') are provided, returns a paginated ResourcePageResponse.
     * When requested without 'page', returns a JSON array of ResourceResponse objects for frontend compatibility.
     */
    @GetMapping
    public ResponseEntity<?> getMyBookmarks(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (page != null) {
            ResourcePageResponse pageResponse = bookmarkService.getMyBookmarksPaginated(principal, page, size);
            return ResponseEntity.ok(pageResponse);
        } else {
            List<ResourceResponse> listResponse = bookmarkService.getMyBookmarksList(principal);
            return ResponseEntity.ok(listResponse);
        }
    }
}
