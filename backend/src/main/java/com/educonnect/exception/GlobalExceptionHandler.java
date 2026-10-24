package com.educonnect.exception;

import com.educonnect.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handler for all REST controllers.
 * Enforces secure, sanitized error responses and prevents information leakage.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles Jakarta Bean Validation errors on @Valid request bodies.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed for submitted request",
                fieldErrors,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles duplicate email conflict during registration.
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(
            DuplicateEmailException ex,
            HttpServletRequest request
    ) {
        logger.warn("Duplicate registration attempt: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                null,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles database constraint violations (e.g., race-condition unique key violations).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        logger.error("Database constraint violation at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                "A conflicting record already exists in the database",
                null,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles invalid login credentials.
     * Uses a generic message to prevent account enumeration.
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            Exception ex,
            HttpServletRequest request
    ) {
        logger.warn("Authentication failed for request to {}: Invalid credentials", request.getRequestURI());

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid email or password",
                null,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handles deactivated or disabled accounts.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleDisabledAccount(
            DisabledException ex,
            HttpServletRequest request
    ) {
        logger.warn("Login attempt for disabled account at {}", request.getRequestURI());

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Your account is currently disabled. Please contact an administrator.",
                null,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handles unauthorized access to RBAC-restricted endpoints.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        logger.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Access denied: You do not possess the required permissions to perform this operation",
                null,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handles explicit ResponseStatusException.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                ex.getStatusCode().value(),
                ex.getReason() != null ? ex.getReason() : "An error occurred",
                null,
                request.getRequestURI()
        );

        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    /**
     * Catch-all fallback for uncaught unexpected exceptions.
     * Never exposes raw stack traces, SQL syntax, or internal server internals.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        logger.error("Unhandled server error at {}: ", request.getRequestURI(), ex);

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected server error occurred. Please try again later.",
                null,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
