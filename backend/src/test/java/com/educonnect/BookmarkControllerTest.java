package com.educonnect;

import com.educonnect.entity.Bookmark;
import com.educonnect.entity.Resource;
import com.educonnect.entity.User;
import com.educonnect.enums.ResourceCategory;
import com.educonnect.enums.ResourceStatus;
import com.educonnect.enums.Role;
import com.educonnect.repository.BookmarkRepository;
import com.educonnect.repository.ResourceRepository;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.JwtTokenProvider;
import com.educonnect.security.UserPrincipal;
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
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User student;
    private String studentToken;
    private Resource approvedResource;
    private Resource pendingResource;

    @BeforeEach
    void setUp() {
        bookmarkRepository.deleteAll();
        resourceRepository.deleteAll();
        userRepository.deleteAll();

        student = new User(
                "Diya Patel",
                "diya@college.edu",
                passwordEncoder.encode("Password123!"),
                "Information Technology",
                "Semester 5",
                Role.STUDENT
        );
        student = userRepository.save(student);

        UserPrincipal principal = UserPrincipal.create(student);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        studentToken = jwtTokenProvider.generateToken(auth);

        approvedResource = new Resource();
        approvedResource.setTitle("Database Management Systems Handout");
        approvedResource.setDescription("Relational algebra and normalization guide.");
        approvedResource.setSubject("DBMS");
        approvedResource.setDepartment("Information Technology");
        approvedResource.setSemester("Semester 4");
        approvedResource.setCategory(ResourceCategory.NOTES);
        approvedResource.setFileType("PDF");
        approvedResource.setFileSize(4096L);
        approvedResource.setFileName("DBMS_Normalization.pdf");
        approvedResource.setStatus(ResourceStatus.APPROVED);
        approvedResource.setUploadedBy(student);
        approvedResource = resourceRepository.save(approvedResource);

        pendingResource = new Resource();
        pendingResource.setTitle("Pending Lab Exercise");
        pendingResource.setDescription("Pending instructor approval.");
        pendingResource.setSubject("Web Dev");
        pendingResource.setDepartment("Information Technology");
        pendingResource.setSemester("Semester 4");
        pendingResource.setCategory(ResourceCategory.LAB);
        pendingResource.setFileType("PDF");
        pendingResource.setFileSize(2048L);
        pendingResource.setFileName("Web_Lab.pdf");
        pendingResource.setStatus(ResourceStatus.PENDING);
        pendingResource.setUploadedBy(student);
        pendingResource = resourceRepository.save(pendingResource);
    }

    @AfterEach
    void tearDown() {
        bookmarkRepository.deleteAll();
        resourceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/bookmarks/{id} - Successfully bookmarks approved resource")
    void testAddBookmark_Success() throws Exception {
        mockMvc.perform(post("/api/bookmarks/{id}", approvedResource.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.resourceId", is(approvedResource.getId().intValue())))
                .andExpect(jsonPath("$.resource.title", is("Database Management Systems Handout")));
    }

    @Test
    @DisplayName("POST /api/bookmarks/{id} - Returns 401 Unauthorized for unauthenticated caller")
    void testAddBookmark_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/bookmarks/{id}", approvedResource.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/bookmarks/{id} - Returns 409 Conflict when duplicate bookmark attempted")
    void testAddBookmark_Duplicate_ReturnsConflict() throws Exception {
        bookmarkRepository.save(new Bookmark(student, approvedResource));

        mockMvc.perform(post("/api/bookmarks/{id}", approvedResource.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("already bookmarked")));
    }

    @Test
    @DisplayName("POST /api/bookmarks/{id} - Returns 400 Bad Request when resource is not approved")
    void testAddBookmark_Unapproved_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/bookmarks/{id}", pendingResource.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Only approved")));
    }

    @Test
    @DisplayName("GET /api/bookmarks/{id}/status - Returns status for bookmarked resource")
    void testCheckBookmarkStatus_Bookmarked() throws Exception {
        bookmarkRepository.save(new Bookmark(student, approvedResource));

        mockMvc.perform(get("/api/bookmarks/{id}/status", approvedResource.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceId", is(approvedResource.getId().intValue())))
                .andExpect(jsonPath("$.bookmarked", is(true)));
    }

    @Test
    @DisplayName("GET /api/bookmarks/{id}/status - Returns status for unbookmarked resource")
    void testCheckBookmarkStatus_NotBookmarked() throws Exception {
        mockMvc.perform(get("/api/bookmarks/{id}/status", approvedResource.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceId", is(approvedResource.getId().intValue())))
                .andExpect(jsonPath("$.bookmarked", is(false)));
    }

    @Test
    @DisplayName("GET /api/bookmarks - Returns JSON array for frontend bookmark table")
    void testGetMyBookmarks_ArrayFormat() throws Exception {
        bookmarkRepository.save(new Bookmark(student, approvedResource));

        mockMvc.perform(get("/api/bookmarks")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(approvedResource.getId().intValue())))
                .andExpect(jsonPath("$[0].title", is("Database Management Systems Handout")));
    }

    @Test
    @DisplayName("GET /api/bookmarks?page=0&size=5 - Returns paginated ResourcePageResponse")
    void testGetMyBookmarks_PaginatedFormat() throws Exception {
        bookmarkRepository.save(new Bookmark(student, approvedResource));

        mockMvc.perform(get("/api/bookmarks")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].id", is(approvedResource.getId().intValue())));
    }

    @Test
    @DisplayName("DELETE /api/bookmarks/{id} - Successfully removes bookmark")
    void testRemoveBookmark_Success() throws Exception {
        bookmarkRepository.save(new Bookmark(student, approvedResource));

        mockMvc.perform(delete("/api/bookmarks/{id}", approvedResource.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("removed successfully")));
    }

    @Test
    @DisplayName("DELETE /api/bookmarks/{id} - Returns 404 Not Found when bookmark does not exist")
    void testRemoveBookmark_NotFound() throws Exception {
        mockMvc.perform(delete("/api/bookmarks/{id}", approvedResource.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Bookmark not found")));
    }
}
