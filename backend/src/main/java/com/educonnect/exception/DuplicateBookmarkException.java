package com.educonnect.exception;

/**
 * Exception thrown when attempting to bookmark a resource that is already saved by the user.
 */
public class DuplicateBookmarkException extends RuntimeException {

    public DuplicateBookmarkException(String message) {
        super(message);
    }

    public DuplicateBookmarkException(Long resourceId) {
        super("Resource with ID " + resourceId + " is already bookmarked.");
    }
}
