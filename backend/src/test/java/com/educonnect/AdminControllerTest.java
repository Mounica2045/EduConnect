package com.educonnect;

import com.educonnect.dto.RejectResourceRequest;
import com.educonnect.dto.UserStatusUpdateRequest;
import com.educonnect.entity.Bookmark;
import com.educonnect.entity.Resource;
import com.educonnect.entity.User;
import com.educonnect.enums.ResourceCategory;
import com.educonnect.enums.ResourceStatus;
import com.educonnect.enums.Role;
import com.educonnect.repository.AdminAuditLogRepository;
import com.educonnect.repository.BookmarkRepository;
import com.educonnect.repository.ResourceRepository;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.JwtTokenProvider;
import com.educonnect.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private AdminAuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private User admin;
    private User student;
    private String adminToken;
    private String studentToken;
    private Resource pendingResource;
    private Resource approvedResource;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        bookmarkRepository.deleteAll();
        resourceRepository.deleteAll();
        userRepository.deleteAll();

        admin = new User(
                "System Admin",
                "admin@educonnect.edu",
                passwordEncoder.encode("AdminPass123!"),
                "Computer Science",
                "Staff",
                Role.ADMIN
        );
        admin = userRepository.save(admin);

        UserPrincipal adminPrincipal = UserPrincipal.create(admin);
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities());
        adminToken = jwtTokenProvider.generateToken(adminAuth);

        student = new User(
                "Test Student",
                "student@educonnect.edu",
                passwordEncoder.encode("StudentPass123!"),
                "Computer Science",
                "Semester 3",
                Role.STUDENT
        );
        student = userRepository.save(student);

        UserPrincipal studentPrincipal = UserPrincipal.create(student);
        Authentication studentAuth = new UsernamePasswordAuthenticationToken(studentPrincipal, null, studentPrincipal.getAuthorities());
        studentToken = jwtTokenProvider.generateToken(studentAuth);

        pendingResource = new Resource();
        pendingResource.setTitle("Compiler Design Notes");
        pendingResource.setDescription("Lexical analysis and parsers.");
        pendingResource.setSubject("Compiler Design");
        pendingResource.setDepartment("Computer Science");
        pendingResource.setSemester("Semester 5");
        pendingResource.setCategory(ResourceCategory.NOTES);
        pendingResource.setFileType("PDF");
        pendingResource.setFileSize(2048L);
        pendingResource.setFileName("Compiler_Notes.pdf");
        pendingResource.setStatus(ResourceStatus.PENDING);
        pendingResource.setUploadedBy(student);
        pendingResource = resourceRepository.save(pendingResource);

        approvedResource = new Resource();
        approvedResource.setTitle("Computer Networks Guide");
        approvedResource.setDescription("OSI model and TCP/IP stack.");
        approvedResource.setSubject("Networks");
        approvedResource.setDepartment("Computer Science");
        approvedResource.setSemester("Semester 4");
        approvedResource.setCategory(ResourceCategory.NOTES);
        approvedResource.setFileType("PDF");
        approvedResource.setFileSize(1024L);
        approvedResource.setFileName("Networks_Guide.pdf");
        approvedResource.setStatus(ResourceStatus.APPROVED);
        approvedResource.setUploadedBy(student);
        approvedResource = resourceRepository.save(approvedResource);
    }

    @AfterEach
    void tearDown() {
        auditLogRepository.deleteAll();
        bookmarkRepository.deleteAll();
        resourceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/admin/dashboard - Successfully returns metrics to admin")
    void testGetDashboardStats_AdminSuccess() throws Exception {
        bookmarkRepository.save(new Bookmark(student, approvedResource));

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", is(2)))
                .andExpect(jsonPath("$.totalResources", is(2)))
                .andExpect(jsonPath("$.approvedResources", is(1)))
                .andExpect(jsonPath("$.pendingResources", is(1)))
                .andExpect(jsonPath("$.rejectedResources", is(0)))
                .andExpect(jsonPath("$.totalBookmarks", is(1)));
    }

    @Test
    @DisplayName("GET /api/admin/dashboard - Rejects non-admin student with 403 Forbidden")
    void testGetDashboardStats_ForbiddenForStudent() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/dashboard - Returns 401 Unauthorized for unauthenticated caller")
    void testGetDashboardStats_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/admin/resources/pending - Returns pending items awaiting review")
    void testGetPendingResources_AdminSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/resources/pending")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(pendingResource.getId().intValue())))
                .andExpect(jsonPath("$.content[0].title", is("Compiler Design Notes")))
                .andExpect(jsonPath("$.content[0].status", is("PENDING")));
    }

    @Test
    @DisplayName("GET /api/admin/resources - Returns array of all resources")
    void testGetAllResources_ArrayFormat() throws Exception {
        mockMvc.perform(get("/api/admin/resources")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("PATCH /api/admin/resources/{id}/approve - Admin approves pending resource")
    void testApproveResource_AdminSuccess() throws Exception {
        mockMvc.perform(patch("/api/admin/resources/{id}/approve", pendingResource.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pendingResource.getId().intValue())))
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.approved", is(true)));
    }

    @Test
    @DisplayName("PATCH /api/admin/resources/{id}/reject - Admin rejects resource with reason")
    void testRejectResource_AdminSuccess() throws Exception {
        RejectResourceRequest request = new RejectResourceRequest("Document contains copyright violations.");

        mockMvc.perform(patch("/api/admin/resources/{id}/reject", pendingResource.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pendingResource.getId().intValue())))
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.approved", is(false)));
    }

    @Test
    @DisplayName("DELETE /api/admin/resources/{id} - Admin deletes resource permanently")
    void testDeleteResource_AdminSuccess() throws Exception {
        mockMvc.perform(delete("/api/admin/resources/{id}", pendingResource.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.id", is(pendingResource.getId().intValue())));
    }

    @Test
    @DisplayName("GET /api/admin/users - Returns paginated user accounts without passwords")
    void testGetUsers_AdminSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].password").doesNotExist());
    }

    @Test
    @DisplayName("PATCH /api/admin/users/{id}/status - Disables student account")
    void testUpdateUserStatus_DisableStudent() throws Exception {
        UserStatusUpdateRequest request = new UserStatusUpdateRequest(false);

        mockMvc.perform(patch("/api/admin/users/{id}/status", student.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(student.getId().intValue())))
                .andExpect(jsonPath("$.enabled", is(false)));
    }

    @Test
    @DisplayName("PATCH /api/admin/users/{id}/status - Self-disable is prevented with 400 Bad Request")
    void testUpdateUserStatus_SelfDisable_Prevented() throws Exception {
        UserStatusUpdateRequest request = new UserStatusUpdateRequest(false);

        mockMvc.perform(patch("/api/admin/users/{id}/status", admin.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Administrators cannot disable their own account")));
    }

    @Test
    @DisplayName("GET /api/admin/audit-logs - Admin views audit log history")
    void testGetAuditLogs_AdminSuccess() throws Exception {
        // Trigger an admin action to populate audit log
        mockMvc.perform(patch("/api/admin/resources/{id}/approve", pendingResource.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/audit-logs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].action", is("APPROVE_RESOURCE")));
    }
}
