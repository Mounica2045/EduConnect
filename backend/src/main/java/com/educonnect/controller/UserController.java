package com.educonnect.controller;

import com.educonnect.dto.UpdateProfileRequest;
import com.educonnect.dto.UserResponse;
import com.educonnect.security.UserPrincipal;
import com.educonnect.service.AuthService;
import com.educonnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user profile operations.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * Get current user profile details.
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        UserResponse response = authService.getCurrentUser(principal);
        return ResponseEntity.ok(response);
    }

    /**
     * Update current authenticated user's profile information.
     * PUT /api/users/me
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserResponse response = userService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok(response);
    }
}
