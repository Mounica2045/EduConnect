package com.educonnect.service;

import com.educonnect.dto.CategoryResponse;
import com.educonnect.dto.ResourceCreateRequest;
import com.educonnect.dto.ResourcePageResponse;
import com.educonnect.dto.ResourceResponse;
import com.educonnect.dto.ResourceUpdateRequest;
import com.educonnect.entity.Resource;
import com.educonnect.entity.User;
import com.educonnect.enums.ResourceCategory;
import com.educonnect.enums.ResourceStatus;
import com.educonnect.enums.Role;
import com.educonnect.exception.ResourceNotFoundException;
import com.educonnect.exception.UnauthorizedResourceAccessException;
import com.educonnect.repository.ResourceRepository;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ResourceService resourceService;

    private User testStudent;
    private User testAdmin;
    private UserPrincipal studentPrincipal;
    private UserPrincipal adminPrincipal;
    private Resource testResource;

    @BeforeEach
    void setUp() {
        testStudent = new User("Aarav Sharma", "student@college.edu", "hash123", "Computer Science & Engineering", "Semester 6", Role.STUDENT);
        testStudent.setId(10L);

        testAdmin = new User("Prof. Lakshmi Rao", "admin@college.edu", "hash123", "Computer Science & Engineering", "Faculty", Role.ADMIN);
        testAdmin.setId(1L);

        studentPrincipal = UserPrincipal.create(testStudent);
        adminPrincipal = UserPrincipal.create(testAdmin);

        testResource = new Resource();
        testResource.setId(100L);
        testResource.setTitle("Data Structures Notes");
        testResource.setDescription("In-depth study notes on Trees and Graphs.");
        testResource.setSubject("Data Structures");
        testResource.setDepartment("Computer Science & Engineering");
        testResource.setSemester("Semester 3");
        testResource.setCategory(ResourceCategory.NOTES);
        testResource.setFileName("DSA_Notes.pdf");
        testResource.setFileType("PDF");
        testResource.setFileSize(2048L);
        testResource.setUploadedBy(testStudent);
        testResource.setStatus(ResourceStatus.APPROVED);
        testResource.setDownloadCount(5);
    }

    @Test
    @DisplayName("searchResources should return filtered PageResponse")
    void testSearchResources() {
        Page<Resource> page = new PageImpl<>(List.of(testResource));
        when(resourceRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ResourcePageResponse response = resourceService.searchResources(
                "Data", "notes", "Computer Science & Engineering", "Semester 3", "Data Structures", "PDF", "recent", 0, 6
        );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Data Structures Notes", response.getContent().get(0).getTitle());
        verify(resourceRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("getResourceById should return approved resource for public caller")
    void testGetResourceById_Approved_Public() {
        when(resourceRepository.findById(100L)).thenReturn(Optional.of(testResource));

        ResourceResponse response = resourceService.getResourceById(100L, null);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Data Structures Notes", response.getTitle());
    }

    @Test
    @DisplayName("getResourceById should throw ResourceNotFoundException for pending resource when unauthenticated")
    void testGetResourceById_Pending_Unauthenticated() {
        testResource.setStatus(ResourceStatus.PENDING);
        when(resourceRepository.findById(100L)).thenReturn(Optional.of(testResource));

        assertThrows(ResourceNotFoundException.class, () -> resourceService.getResourceById(100L, null));
    }

    @Test
    @DisplayName("getResourceById should allow uploader to view their own pending resource")
    void testGetResourceById_Pending_Owner() {
        testResource.setStatus(ResourceStatus.PENDING);
        when(resourceRepository.findById(100L)).thenReturn(Optional.of(testResource));

        ResourceResponse response = resourceService.getResourceById(100L, studentPrincipal);

        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
    }

    @Test
    @DisplayName("createResource should set PENDING status for student submissions")
    void testCreateResource_StudentSubmission() {
        ResourceCreateRequest request = new ResourceCreateRequest(
                "Operating Systems Lab Guide",
                "Full practical exercises with shell scripts and Linux commands.",
                "Operating Systems",
                "Computer Science & Engineering",
                "Semester 4",
                "lab",
                null,
                "os, linux, lab"
        );

        MockMultipartFile file = new MockMultipartFile(
                "file", "OS_Lab.pdf", "application/pdf", "dummy pdf content".getBytes()
        );

        when(userRepository.findById(10L)).thenReturn(Optional.of(testStudent));
        when(fileStorageService.storeFile(file)).thenReturn(
                new FileStorageService.StoredFile("OS_Lab.pdf", "/uploads/uuid_OS_Lab.pdf", "PDF", 1024L)
        );
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
            Resource r = invocation.getArgument(0);
            r.setId(101L);
            return r;
        });

        ResourceResponse response = resourceService.createResource(request, file, studentPrincipal);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("PENDING", response.getStatus());
        assertFalse(response.isApproved());
        assertEquals("OS_Lab.pdf", response.getFileName());
        verify(fileStorageService, times(1)).storeFile(file);
    }

    @Test
    @DisplayName("createResource should set APPROVED status for administrator submissions")
    void testCreateResource_AdminSubmission() {
        ResourceCreateRequest request = new ResourceCreateRequest(
                "Syllabus & Course Regulations",
                "Official university curriculum regulations and marking scheme.",
                "Curriculum",
                "Computer Science & Engineering",
                "All Semesters",
                "other",
                "https://college.edu/syllabus",
                "curriculum, regulations"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
            Resource r = invocation.getArgument(0);
            r.setId(102L);
            return r;
        });

        ResourceResponse response = resourceService.createResource(request, null, adminPrincipal);

        assertNotNull(response);
        assertEquals(102L, response.getId());
        assertEquals("APPROVED", response.getStatus());
        assertTrue(response.isApproved());
    }

    @Test
    @DisplayName("updateResource should update fields when caller is the owner")
    void testUpdateResource_OwnerSuccess() {
        when(resourceRepository.findById(100L)).thenReturn(Optional.of(testResource));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(inv -> inv.getArgument(0));

        ResourceUpdateRequest updateReq = new ResourceUpdateRequest();
        updateReq.setTitle("Updated DSA Notes v2");
        updateReq.setDescription("Updated with AVL tree examples and code snippets.");

        ResourceResponse response = resourceService.updateResource(100L, updateReq, studentPrincipal);

        assertNotNull(response);
        assertEquals("Updated DSA Notes v2", response.getTitle());
    }

    @Test
    @DisplayName("updateResource should reject modifications from non-owners")
    void testUpdateResource_UnauthorizedOtherStudent() {
        User otherStudent = new User("Pooja Verma", "pooja@college.edu", "hash123", "Information Technology", "Semester 6", Role.STUDENT);
        otherStudent.setId(99L);
        UserPrincipal otherPrincipal = UserPrincipal.create(otherStudent);

        when(resourceRepository.findById(100L)).thenReturn(Optional.of(testResource));

        ResourceUpdateRequest updateReq = new ResourceUpdateRequest();
        updateReq.setTitle("Malicious Title Override");

        assertThrows(UnauthorizedResourceAccessException.class, () ->
                resourceService.updateResource(100L, updateReq, otherPrincipal)
        );
    }

    @Test
    @DisplayName("approveResource should change status to APPROVED")
    void testApproveResource() {
        testResource.setStatus(ResourceStatus.PENDING);
        when(resourceRepository.findById(100L)).thenReturn(Optional.of(testResource));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(inv -> inv.getArgument(0));

        ResourceResponse response = resourceService.approveResource(100L);

        assertNotNull(response);
        assertEquals("APPROVED", response.getStatus());
        assertTrue(response.isApproved());
    }

    @Test
    @DisplayName("getCategories should return complete category list")
    void testGetCategories() {
        List<CategoryResponse> categories = resourceService.getCategories();
        assertNotNull(categories);
        assertEquals(ResourceCategory.values().length, categories.size());
        assertTrue(categories.stream().anyMatch(c -> c.getId().equals("notes")));
        assertTrue(categories.stream().anyMatch(c -> c.getId().equals("qp")));
    }
}
