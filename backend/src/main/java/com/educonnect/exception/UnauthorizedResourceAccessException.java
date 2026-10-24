package com.educonnect.exception;

/**
 * Exception thrown when a user attempts to edit or delete a resource they do not own.
 */
public class UnauthorizedResourceAccessException extends RuntimeException {

    public UnauthorizedResourceAccessException(String message) {
        super(message);
    }
}
