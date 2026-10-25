package com.educonnect;

import com.educonnect.dto.LoginRequest;
import com.educonnect.dto.RegisterRequest;
import com.educonnect.entity.User;
import com.educonnect.enums.Role;
import com.educonnect.repository.BookmarkRepository;
import com.educonnect.repository.ResourceRepository;
import com.educonnect.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private ResourceRepository resourceRepository;

    @Autowired(required = false)
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        if (bookmarkRepository != null) bookmarkRepository.deleteAll();
        if (resourceRepository != null) resourceRepository.deleteAll();
        userRepository.deleteAll();

        // Seed one verified user for login tests
        User testStudent = new User(
                "Test Student",
                "existing@college.edu",
                passwordEncoder.encode("ValidPassword123!"),
                "Computer Science",
                "Semester 4",
                Role.STUDENT
        );
        userRepository.save(testStudent);
    }

    @AfterEach
    void tearDown() {
        if (bookmarkRepository != null) bookmarkRepository.deleteAll();
        if (resourceRepository != null) resourceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 201 Created on valid registration")
    void testRegister_Success_Returns201() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "New Student",
                "newstudent@college.edu",
                "SecurePassword123!",
                "Information Technology",
                "Semester 2"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Registration successful")))
                .andExpect(jsonPath("$.user.fullName", is("New Student")))
                .andExpect(jsonPath("$.user.email", is("newstudent@college.edu")))
                .andExpect(jsonPath("$.user.role", is("STUDENT")))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 Bad Request on missing fields")
    void testRegister_MissingFields_Returns400() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "",
                "invalid-email",
                "short",
                "Computer Science",
                "Semester 4"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 409 Conflict on duplicate email")
    void testRegister_DuplicateEmail_Returns409() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Duplicate Student",
                "existing@college.edu",
                "ValidPassword123!",
                "Computer Science",
                "Semester 4"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 200 OK and JWT token on valid credentials")
    void testLogin_Success_ReturnsToken() throws Exception {
        LoginRequest request = new LoginRequest("existing@college.edu", "ValidPassword123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.user.email", is("existing@college.edu")))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 401 Unauthorized on invalid password")
    void testLogin_InvalidCredentials_Returns401() throws Exception {
        LoginRequest request = new LoginRequest("existing@college.edu", "WrongPassword999!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Invalid email or password")));
    }

    @Test
    @DisplayName("GET /api/auth/me - Should return 401 Unauthorized when unauthenticated")
    void testGetCurrentUser_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("POST /api/auth/logout - Should return 200 OK")
    void testLogout_Success() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Logged out successfully")));
    }
}
