package com.educonnect.service;

import com.educonnect.dto.RegisterRequest;
import com.educonnect.dto.UserResponse;
import com.educonnect.entity.User;
import com.educonnect.enums.Role;
import com.educonnect.exception.DuplicateEmailException;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "Aarav Sharma",
                "  AARAV@college.edu  ",
                "Password123!",
                "Computer Science",
                "Semester 4"
        );
    }

    @Test
    @DisplayName("Should successfully register student with hashed password and STUDENT role")
    void testRegister_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$12$hashedPasswordExample");

        User savedUser = new User(
                "Aarav Sharma",
                "aarav@college.edu",
                "$2a$12$hashedPasswordExample",
                "Computer Science",
                "Semester 4",
                Role.STUDENT
        );
        savedUser.setId(101L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("Aarav Sharma", response.getFullName());
        assertEquals("aarav@college.edu", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());

        // Verify password hashing was invoked
        verify(passwordEncoder, times(1)).encode("Password123!");
        // Verify user repository save was invoked
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email is already registered")
    void testRegister_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail("aarav@college.edu")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> {
            authService.register(registerRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should normalize email to lowercase without whitespace")
    void testEmailNormalization() {
        assertEquals("aarav@college.edu", registerRequest.getEmail());
    }
}
