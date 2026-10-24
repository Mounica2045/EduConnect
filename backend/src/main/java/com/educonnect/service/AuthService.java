package com.educonnect.service;

import com.educonnect.dto.AuthResponse;
import com.educonnect.dto.LoginRequest;
import com.educonnect.dto.RegisterRequest;
import com.educonnect.dto.UserResponse;
import com.educonnect.entity.User;
import com.educonnect.enums.Role;
import com.educonnect.exception.DuplicateEmailException;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.JwtTokenProvider;
import com.educonnect.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user registration, authentication, token issuance, and identity retrieval.
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Registers a new student account.
     * Enforces unique email check, password hashing, and server-side STUDENT role assignment.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("An account with email " + normalizedEmail + " already exists.");
        }

        // Hash plaintext password using BCrypt
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // Unconditionally assign STUDENT role on server to prevent privilege escalation
        User user = new User(
                request.getFullName(),
                normalizedEmail,
                passwordHash,
                request.getDepartment(),
                request.getSemester(),
                Role.STUDENT
        );

        User savedUser = userRepository.save(user);
        logger.info("New student registered successfully with ID: {}", savedUser.getId());

        return UserResponse.from(savedUser);
    }

    /**
     * Authenticates credentials and returns a secure JWT token with user identity.
     */
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findByEmail(userPrincipal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));

        logger.info("User {} logged in successfully", user.getEmail());

        return new AuthResponse(
                "Login successful",
                jwt,
                UserResponse.from(user)
        );
    }

    /**
     * Retrieves the profile DTO for the currently authenticated user principal.
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalStateException("Current user not found with ID: " + userPrincipal.getId()));

        return UserResponse.from(user);
    }
}
