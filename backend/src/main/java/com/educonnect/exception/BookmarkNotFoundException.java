package com.educonnect.exception;

/**
 * Exception thrown when a requested bookmark does not exist for the user.
 */
public class BookmarkNotFoundException extends RuntimeException {

    public BookmarkNotFoundException(String message) {
        super(message);
    }

    public BookmarkNotFoundException(Long resourceId) {
        super("Bookmark not found for resource ID: " + resourceId);
    }
}
