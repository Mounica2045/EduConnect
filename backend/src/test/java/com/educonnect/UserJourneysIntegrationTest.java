package com.educonnect;

import com.educonnect.dto.LoginRequest;
import com.educonnect.dto.RegisterRequest;
import com.educonnect.dto.RejectResourceRequest;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-End User Journeys Integration Test Suite.
 * Validates complete end-to-end user workflows across all 5 member modules.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class UserJourneysIntegrationTest {

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

    private User adminUser;
    private String adminToken;

    @BeforeEach
    void setUp() {
        tearDown();

        adminUser = new User(
                "Prof. Lakshmi Rao",
                "admin.lakshmi@college.edu",
                passwordEncoder.encode("AdminPass123!"),
                "Computer Science & Engineering",
                "Faculty",
                Role.ADMIN
        );
        adminUser = userRepository.save(adminUser);

        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities());
        adminToken = jwtTokenProvider.generateToken(adminAuth);
    }

    @AfterEach
    void tearDown() {
        auditLogRepository.deleteAll();
        bookmarkRepository.deleteAll();
        resourceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Journey A — Student registration, validation, login, me check, and invalid credentials rejection")
    void testJourneyA_StudentRegistrationAndLogin() throws Exception {
        // 1. Validation check on missing fields
        RegisterRequest invalidReq = new RegisterRequest("", "bad-email", "123", "", "");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists());

        // 2. Valid Registration
        RegisterRequest validReq = new RegisterRequest(
                "Vikram Varma",
                "vikram.varma@college.edu",
                "SecureStudentPass123!",
                "Information Technology",
                "Semester 4"
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", containsString("successful")))
                .andExpect(jsonPath("$.user.email", is("vikram.varma@college.edu")))
                .andExpect(jsonPath("$.user.role", is("STUDENT")));

        // 3. Duplicate email handling
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("already exists")));

        // 4. Invalid Login Credentials rejection
        LoginRequest badLogin = new LoginRequest("vikram.varma@college.edu", "WrongPassword999!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));

        // 5. Valid Login
        LoginRequest validLogin = new LoginRequest("vikram.varma@college.edu", "SecureStudentPass123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // 6. Access protected current user identity (/api/auth/me)
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("vikram.varma@college.edu")))
                .andExpect(jsonPath("$.role", is("STUDENT")));
    }

    @Test
    @DisplayName("Journey B — Resource discovery, multi-criteria filtering, and public approval visibility rules")
    void testJourneyB_ResourceDiscoveryAndFiltering() throws Exception {
        User author = new User("Pooja Nair", "pooja.nair@college.edu", passwordEncoder.encode("Pass123!"), "CS", "Sem 4", Role.STUDENT);
        author = userRepository.save(author);

        Resource approvedDsa = new Resource();
        approvedDsa.setTitle("Advanced Data Structures & Algorithms");
        approvedDsa.setDescription("Red-black trees and graph traversal algorithms.");
        approvedDsa.setSubject("Data Structures");
        approvedDsa.setDepartment("Computer Science & Engineering");
        approvedDsa.setSemester("Semester 3");
        approvedDsa.setCategory(ResourceCategory.NOTES);
        approvedDsa.setStatus(ResourceStatus.APPROVED);
        approvedDsa.setUploadedBy(author);
        resourceRepository.save(approvedDsa);

        Resource approvedDbms = new Resource();
        approvedDbms.setTitle("Database Systems Lab Manual");
        approvedDbms.setDescription("SQL queries and normalization lab exercises.");
        approvedDbms.setSubject("DBMS");
        approvedDbms.setDepartment("Information Technology");
        approvedDbms.setSemester("Semester 4");
        approvedDbms.setCategory(ResourceCategory.LAB);
        approvedDbms.setStatus(ResourceStatus.APPROVED);
        approvedDbms.setUploadedBy(author);
        resourceRepository.save(approvedDbms);

        Resource pendingDraft = new Resource();
        pendingDraft.setTitle("Unmoderated Draft Question Paper");
        pendingDraft.setDescription("Pending paper for moderation.");
        pendingDraft.setSubject("Physics");
        pendingDraft.setDepartment("Basic Sciences & Humanities");
        pendingDraft.setSemester("Semester 1");
        pendingDraft.setCategory(ResourceCategory.QP);
        pendingDraft.setStatus(ResourceStatus.PENDING);
        pendingDraft.setUploadedBy(author);
        resourceRepository.save(pendingDraft);

        // 1. Public listing only returns APPROVED resources (draft excluded)
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content", hasSize(2)));

        // 2. Keyword Search
        mockMvc.perform(get("/api/resources").param("q", "Red-black trees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].title", is("Advanced Data Structures & Algorithms")));

        // 3. Faceted combination: category=lab and department=Information Technology
        mockMvc.perform(get("/api/resources")
                        .param("category", "lab")
                        .param("department", "Information Technology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].title", is("Database Systems Lab Manual")));

        // 4. Detail page for approved resource
        mockMvc.perform(get("/api/resources/{id}", approvedDsa.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Advanced Data Structures & Algorithms")));

        // 5. Unauthenticated detail request for pending draft returns 404 to prevent enumeration
        mockMvc.perform(get("/api/resources/{id}", pendingDraft.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("pending administrator verification")));
    }

    @Test
    @DisplayName("Journey C — Student resource submission, pending status, and self-approval prevention")
    void testJourneyC_ResourceSubmissionWorkflow() throws Exception {
        User student = new User("Rahul Verma", "rahul.verma@college.edu", passwordEncoder.encode("Pass123!"), "CS", "Sem 4", Role.STUDENT);
        student = userRepository.save(student);
        UserPrincipal principal = UserPrincipal.create(student);
        String studentToken = jwtTokenProvider.generateToken(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        MockMultipartFile file = new MockMultipartFile("file", "OS_Notes.pdf", "application/pdf", "Dummy OS PDF".getBytes());

        // 1. Submit resource via multipart
        MvcResult uploadResult = mockMvc.perform(multipart("/api/resources")
                        .file(file)
                        .param("title", "Operating Systems Lecture Handbook")
                        .param("description", "Comprehensive notes on process scheduling.")
                        .param("subject", "Operating Systems")
                        .param("department", "Computer Science & Engineering")
                        .param("semester", "Semester 4")
                        .param("category", "notes")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.approved", is(false)))
                .andReturn();

        long createdId = objectMapper.readTree(uploadResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Student attempts to self-approve via PATCH -> 403 Forbidden
        mockMvc.perform(patch("/api/resources/{id}/approve", createdId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        // 3. Admin successfully approves -> 200 OK
        mockMvc.perform(patch("/api/admin/resources/{id}/approve", createdId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.approved", is(true)));
    }

    @Test
    @DisplayName("Journey D — Administration: dashboard metrics, moderation rejection, and user status toggling")
    void testJourneyD_AdminDashboardAndModeration() throws Exception {
        User student = new User("Siddharth Roy", "sid.roy@college.edu", passwordEncoder.encode("Pass123!"), "IT", "Sem 6", Role.STUDENT);
        student = userRepository.save(student);

        Resource draft = new Resource();
        draft.setTitle("Incomplete Draft Notes");
        draft.setDescription("Needs more examples.");
        draft.setSubject("Web Tech");
        draft.setDepartment("IT");
        draft.setSemester("Sem 6");
        draft.setCategory(ResourceCategory.NOTES);
        draft.setStatus(ResourceStatus.PENDING);
        draft.setUploadedBy(student);
        draft = resourceRepository.save(draft);

        // 1. Admin inspects dashboard metrics
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", is(2)))
                .andExpect(jsonPath("$.pendingResources", is(1)));

        // 2. Admin rejects submission with reason
        RejectResourceRequest rejectReq = new RejectResourceRequest("Document lacks required chapter solutions.");
        mockMvc.perform(patch("/api/admin/resources/{id}/reject", draft.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")));

        // 3. Admin audits verify action was logged
        mockMvc.perform(get("/api/admin/audit-logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action", is("REJECT_RESOURCE")))
                .andExpect(jsonPath("$.content[0].details", containsString("Document lacks required chapter solutions")));

        // 4. Admin disables a user account
        mockMvc.perform(patch("/api/admin/users/{id}/status", student.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(false)));
    }

    @Test
    @DisplayName("Journey E — Bookmarks: add, status check, duplicate prevention, list, and removal")
    void testJourneyE_StudentBookmarksWorkflow() throws Exception {
        User student = new User("Meera Joshi", "meera.joshi@college.edu", passwordEncoder.encode("Pass123!"), "CS", "Sem 5", Role.STUDENT);
        student = userRepository.save(student);
        UserPrincipal principal = UserPrincipal.create(student);
        String studentToken = jwtTokenProvider.generateToken(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        Resource approvedBook = new Resource();
        approvedBook.setTitle("Cloud Computing Architecture");
        approvedBook.setDescription("AWS, GCP, and Azure reference models.");
        approvedBook.setSubject("Cloud Computing");
        approvedBook.setDepartment("Computer Science");
        approvedBook.setSemester("Semester 6");
        approvedBook.setCategory(ResourceCategory.NOTES);
        approvedBook.setStatus(ResourceStatus.APPROVED);
        approvedBook.setUploadedBy(adminUser);
        approvedBook = resourceRepository.save(approvedBook);

        // 1. Initial status is not bookmarked
        mockMvc.perform(get("/api/bookmarks/{id}/status", approvedBook.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarked", is(false)));

        // 2. Add bookmark
        mockMvc.perform(post("/api/bookmarks/{id}", approvedBook.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resourceId", is(approvedBook.getId().intValue())));

        // 3. Duplicate bookmark attempt is rejected with 409 Conflict
        mockMvc.perform(post("/api/bookmarks/{id}", approvedBook.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));

        // 4. List user's bookmarks (array format)
        mockMvc.perform(get("/api/bookmarks")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Cloud Computing Architecture")));

        // 5. Remove bookmark
        mockMvc.perform(delete("/api/bookmarks/{id}", approvedBook.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // 6. Verify removed
        mockMvc.perform(get("/api/bookmarks")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Journey F — Access control: unauthenticated access rejection and role separation")
    void testJourneyF_AccessControlAndSecurity() throws Exception {
        // 1. Unauthenticated access to protected routes returns 401
        mockMvc.perform(get("/api/resources/mine"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));

        mockMvc.perform(get("/api/bookmarks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));

        // 2. Student user cannot access admin routes -> 403 Forbidden
        User student = new User("Student A", "student.a@college.edu", passwordEncoder.encode("Pass123!"), "CS", "Sem 1", Role.STUDENT);
        student = userRepository.save(student);
        UserPrincipal principal = UserPrincipal.create(student);
        String studentToken = jwtTokenProvider.generateToken(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }
}
