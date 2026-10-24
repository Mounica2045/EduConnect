package com.educonnect.exception;

/**
 * Thrown when registration is attempted with an already registered email address.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
