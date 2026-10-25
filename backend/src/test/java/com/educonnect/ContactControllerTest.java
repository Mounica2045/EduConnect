package com.educonnect;

import com.educonnect.dto.ContactRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/contact - Publicly accepts valid contact inquiry")
    void testSubmitInquiry_Success() throws Exception {
        ContactRequest request = new ContactRequest(
                "Ananya Reddy",
                "ananya.reddy@college.edu",
                "Question about Lab Manuals",
                "Could you please add the latest DBMS lab manual for Semester 4?"
        );

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("inquiry has been received")))
                .andExpect(jsonPath("$.sender", is("Ananya Reddy")));
    }

    @Test
    @DisplayName("POST /api/contact - Rejects missing fields with 400 Bad Request")
    void testSubmitInquiry_InvalidValidation() throws Exception {
        ContactRequest request = new ContactRequest(
                "",
                "not-an-email",
                "",
                "Short"
        );

        mockMvc.perform(post("/api/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.subject").exists())
                .andExpect(jsonPath("$.errors.message").exists());
    }
}
