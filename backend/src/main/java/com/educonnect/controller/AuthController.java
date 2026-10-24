package com.educonnect.controller;

import com.educonnect.dto.AuthResponse;
import com.educonnect.dto.LoginRequest;
import com.educonnect.dto.RegisterRequest;
import com.educonnect.dto.UserResponse;
import com.educonnect.security.UserPrincipal;
import com.educonnect.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for user registration, authentication, token issuance, and current identity.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Public student registration endpoint.
     * POST /api/auth/register -> HTTP 201 Created
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registration successful");
        response.put("user", user);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * User login endpoint returning signed JWT Bearer token.
     * POST /api/auth/login -> HTTP 200 OK
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * User logout endpoint. Clears security context on server.
     * POST /api/auth/logout -> HTTP 200 OK
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Collections.singletonMap("message", "Logged out successfully"));
    }

    /**
     * Returns identity details of currently authenticated user.
     * GET /api/auth/me -> HTTP 200 OK
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse user = authService.getCurrentUser(principal);
        return ResponseEntity.ok(user);
    }
}
