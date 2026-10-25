package com.educonnect;

import com.educonnect.entity.Resource;
import com.educonnect.entity.User;
import com.educonnect.enums.ResourceCategory;
import com.educonnect.enums.ResourceStatus;
import com.educonnect.enums.Role;
import com.educonnect.repository.ResourceRepository;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired(required = false)
    private com.educonnect.repository.BookmarkRepository bookmarkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private User testStudent;
    private User testAdmin;
    private String studentToken;
    private String adminToken;
    private Resource approvedResource;
    private Resource pendingResource;

    @BeforeEach
    void setUp() {
        if (bookmarkRepository != null) bookmarkRepository.deleteAll();
        resourceRepository.deleteAll();
        userRepository.deleteAll();

        testStudent = new User(
                "Aarav Sharma",
                "student@college.edu",
                passwordEncoder.encode("Password123!"),
                "Computer Science",
                "Semester 6",
                Role.STUDENT
        );
        testStudent = userRepository.save(testStudent);
        com.educonnect.security.UserPrincipal studentPrincipal = com.educonnect.security.UserPrincipal.create(testStudent);
        org.springframework.security.core.Authentication studentAuth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(studentPrincipal, null, studentPrincipal.getAuthorities());
        studentToken = jwtTokenProvider.generateToken(studentAuth);

        testAdmin = new User(
                "Prof. Lakshmi Rao",
                "admin@college.edu",
                passwordEncoder.encode("AdminPassword123!"),
                "Computer Science",
                "Faculty",
                Role.ADMIN
        );
        testAdmin = userRepository.save(testAdmin);
        com.educonnect.security.UserPrincipal adminPrincipal = com.educonnect.security.UserPrincipal.create(testAdmin);
        org.springframework.security.core.Authentication adminAuth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities());
        adminToken = jwtTokenProvider.generateToken(adminAuth);

        approvedResource = new Resource();
        approvedResource.setTitle("Data Structures and Algorithms Notes");
        approvedResource.setDescription("Comprehensive notes on graph algorithms and dynamic programming.");
        approvedResource.setSubject("Data Structures");
        approvedResource.setDepartment("Computer Science");
        approvedResource.setSemester("Semester 3");
        approvedResource.setCategory(ResourceCategory.NOTES);
        approvedResource.setFileType("PDF");
        approvedResource.setFileSize(2048L);
        approvedResource.setFileName("DSA_Notes.pdf");
        approvedResource.setStatus(ResourceStatus.APPROVED);
        approvedResource.setUploadedBy(testStudent);
        approvedResource = resourceRepository.save(approvedResource);

        pendingResource = new Resource();
        pendingResource.setTitle("Unmoderated Draft Document");
        pendingResource.setDescription("Pending submission for review.");
        pendingResource.setSubject("Database Systems");
        pendingResource.setDepartment("Computer Science");
        pendingResource.setSemester("Semester 4");
        pendingResource.setCategory(ResourceCategory.LAB);
        pendingResource.setFileType("PDF");
        pendingResource.setFileSize(1024L);
        pendingResource.setFileName("DBMS_Draft.pdf");
        pendingResource.setStatus(ResourceStatus.PENDING);
        pendingResource.setUploadedBy(testStudent);
        pendingResource = resourceRepository.save(pendingResource);
    }

    @AfterEach
    void tearDown() {
        if (bookmarkRepository != null) bookmarkRepository.deleteAll();
        resourceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/resources - Publicly accessible and returns approved resources page")
    void testSearchResources_PublicAccess() throws Exception {
        mockMvc.perform(get("/api/resources")
                        .param("q", "Data Structures")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Data Structures and Algorithms Notes")))
                .andExpect(jsonPath("$.content[0].category", is("notes")))
                .andExpect(jsonPath("$.content[0].status", is("APPROVED")))
                .andExpect(jsonPath("$.content[0].approved", is(true)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("GET /api/resources/recent - Returns latest approved resources")
    void testGetRecentResources() throws Exception {
        mockMvc.perform(get("/api/resources/recent")
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Data Structures and Algorithms Notes")));
    }

    @Test
    @DisplayName("GET /api/resources/categories - Returns category list with slugs and badge classes")
    void testGetCategories() throws Exception {
        mockMvc.perform(get("/api/resources/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(ResourceCategory.values().length)))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].badgeClass", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/resources/{id} - Returns details of approved resource")
    void testGetResourceById_Approved() throws Exception {
        mockMvc.perform(get("/api/resources/{id}", approvedResource.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(approvedResource.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Data Structures and Algorithms Notes")))
                .andExpect(jsonPath("$.contributorName", is("Aarav Sharma")));
    }

    @Test
    @DisplayName("GET /api/resources/{id} - Returns 404 for non-existent ID")
    void testGetResourceById_NotFound() throws Exception {
        mockMvc.perform(get("/api/resources/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Resource not found")));
    }

    @Test
    @DisplayName("GET /api/resources/mine - Returns 401 Unauthorized for unauthenticated caller")
    void testGetMyResources_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/resources/mine")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/resources/mine - Returns uploads for authenticated user")
    void testGetMyResources_Authenticated() throws Exception {
        mockMvc.perform(get("/api/resources/mine")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2))); // Includes both approved and pending for the owner
    }

    @Test
    @DisplayName("POST /api/resources - Returns 401 Unauthorized for unauthenticated upload")
    void testCreateResource_Unauthenticated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "Test.pdf", "application/pdf", "dummy file content".getBytes()
        );

        mockMvc.perform(multipart("/api/resources")
                        .file(file)
                        .param("title", "Unauthorized Upload")
                        .param("description", "Valid description text over 10 chars")
                        .param("subject", "Math")
                        .param("department", "CSE")
                        .param("category", "notes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/resources - Successfully creates resource with multipart file and queues as PENDING")
    void testCreateResource_MultipartSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "Operating_Systems.pdf", "application/pdf", "Dummy OS PDF content".getBytes()
        );

        mockMvc.perform(multipart("/api/resources")
                        .file(file)
                        .param("title", "Operating Systems Lecture Notes")
                        .param("description", "Comprehensive notes on CPU scheduling and deadlocks.")
                        .param("subject", "Operating Systems")
                        .param("department", "Computer Science")
                        .param("semester", "Semester 4")
                        .param("category", "notes")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Operating Systems Lecture Notes")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.approved", is(false)));
    }

    @Test
    @DisplayName("PATCH /api/resources/{id}/approve - Rejects non-admin students with 403 Forbidden")
    void testApproveResource_ForbiddenForStudent() throws Exception {
        mockMvc.perform(patch("/api/resources/{id}/approve", pendingResource.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/resources/{id}/approve - Admin successfully approves resource")
    void testApproveResource_AdminSuccess() throws Exception {
        mockMvc.perform(patch("/api/resources/{id}/approve", pendingResource.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pendingResource.getId().intValue())))
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.approved", is(true)));
    }
}
