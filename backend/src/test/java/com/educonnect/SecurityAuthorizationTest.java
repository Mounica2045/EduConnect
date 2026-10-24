package com.educonnect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Admin Endpoints - Should deny student access with 403 Forbidden")
    @WithMockUser(username = "student@college.edu", roles = {"STUDENT"})
    void testStudentAccessToAdminEndpoint_Returns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/resources"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Admin Endpoints - Should return 401 for unauthenticated user")
    void testUnauthenticatedAccessToAdminEndpoint_Returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/resources"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Public Endpoints - Resources read-only should permit all")
    void testPublicResources_PermitAll() throws Exception {
        // GET /api/resources should not fail with 401 or 403
        mockMvc.perform(get("/api/resources"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // May return 404 if resources controller is not yet mounted by Member 3, but NOT 401/403
                    assert status != 401 && status != 403;
                });
    }
}
