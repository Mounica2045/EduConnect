package com.educonnect.controller;

import com.educonnect.dto.ContactRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for community student inquiries and portal feedback.
 */
@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    /**
     * POST /api/contact
     * Receives inquiry or feedback from students and community members.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> submitInquiry(@Valid @RequestBody ContactRequest request) {
        logger.info("Received community inquiry from: {} <{}> | Subject: {}",
                request.getName(), request.getEmail(), request.getSubject());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Your inquiry has been received. Thank you for contacting the EduConnect team!");
        response.put("sender", request.getName());

        return ResponseEntity.ok(response);
    }
}
