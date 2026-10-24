package com.educonnect.exception;

/**
 * Exception thrown when an educational resource is not found or is not accessible.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(Long id) {
        super("Resource not found with ID: " + id);
    }
}
