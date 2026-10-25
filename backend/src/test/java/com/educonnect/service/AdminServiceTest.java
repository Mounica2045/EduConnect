package com.educonnect.service;

import com.educonnect.dto.AdminDashboardStatsResponse;
import com.educonnect.dto.ResourceResponse;
import com.educonnect.dto.UserResponse;
import com.educonnect.entity.AdminAuditLog;
import com.educonnect.entity.Resource;
import com.educonnect.entity.User;
import com.educonnect.enums.ResourceCategory;
import com.educonnect.enums.ResourceStatus;
import com.educonnect.enums.Role;
import com.educonnect.exception.ResourceNotFoundException;
import com.educonnect.repository.AdminAuditLogRepository;
import com.educonnect.repository.BookmarkRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private AdminAuditLogRepository auditLogRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private AdminService adminService;

    private User adminUser;
    private User studentUser;
    private UserPrincipal adminPrincipal;
    private Resource pendingResource;

    @BeforeEach
    void setUp() {
        adminUser = new User("Admin User", "admin@college.edu", "hashedPass", "CS", "Faculty", Role.ADMIN);
        adminUser.setId(1L);
        adminPrincipal = UserPrincipal.create(adminUser);

        studentUser = new User("Student User", "student@college.edu", "hashedPass", "CS", "Sem 4", Role.STUDENT);
        studentUser.setId(2L);
        studentUser.setEnabled(true);

        pendingResource = new Resource();
        pendingResource.setId(50L);
        pendingResource.setTitle("Algorithms Notes");
        pendingResource.setStatus(ResourceStatus.PENDING);
        pendingResource.setCategory(ResourceCategory.NOTES);
        pendingResource.setUploadedBy(studentUser);
        pendingResource.setFileLocation("uploads/algo.pdf");
    }

    @Test
    @DisplayName("getDashboardStats should aggregate all counts correctly")
    void testGetDashboardStats() {
        when(userRepository.count()).thenReturn(25L);
        when(resourceRepository.count()).thenReturn(100L);
        when(resourceRepository.countByStatus(ResourceStatus.APPROVED)).thenReturn(80L);
        when(resourceRepository.countByStatus(ResourceStatus.PENDING)).thenReturn(15L);
        when(resourceRepository.countByStatus(ResourceStatus.REJECTED)).thenReturn(5L);
        when(bookmarkRepository.count()).thenReturn(42L);

        AdminDashboardStatsResponse stats = adminService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(25L, stats.getTotalUsers());
        assertEquals(100L, stats.getTotalResources());
        assertEquals(80L, stats.getApprovedResources());
        assertEquals(15L, stats.getPendingResources());
        assertEquals(5L, stats.getRejectedResources());
        assertEquals(42L, stats.getTotalBookmarks());
    }

    @Test
    @DisplayName("approveResource should update status to APPROVED and log audit event")
    void testApproveResource_Success() {
        when(resourceRepository.findById(50L)).thenReturn(Optional.of(pendingResource));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(inv -> inv.getArgument(0));

        ResourceResponse response = adminService.approveResource(50L, adminPrincipal);

        assertNotNull(response);
        assertEquals("APPROVED", response.getStatus());
        assertTrue(response.isApproved());
        verify(resourceRepository, times(1)).save(pendingResource);
        verify(auditLogRepository, times(1)).save(any(AdminAuditLog.class));
    }

    @Test
    @DisplayName("approveResource should throw ResourceNotFoundException for invalid ID")
    void testApproveResource_NotFound_ThrowsException() {
        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                adminService.approveResource(999L, adminPrincipal)
        );

        verify(resourceRepository, never()).save(any(Resource.class));
        verify(auditLogRepository, never()).save(any(AdminAuditLog.class));
    }

    @Test
    @DisplayName("rejectResource should update status to REJECTED and record reason in audit log")
    void testRejectResource_Success() {
        when(resourceRepository.findById(50L)).thenReturn(Optional.of(pendingResource));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(inv -> inv.getArgument(0));

        ResourceResponse response = adminService.rejectResource(50L, "Incomplete content", adminPrincipal);

        assertNotNull(response);
        assertEquals("REJECTED", response.getStatus());
        assertFalse(response.isApproved());
        verify(resourceRepository, times(1)).save(pendingResource);
        verify(auditLogRepository, times(1)).save(argThat(log ->
                "REJECT_RESOURCE".equals(log.getAction()) &&
                log.getDetails().contains("Incomplete content")
        ));
    }

    @Test
    @DisplayName("deleteResource should remove file and resource entity and log audit action")
    void testDeleteResource_Success() {
        when(resourceRepository.findById(50L)).thenReturn(Optional.of(pendingResource));

        adminService.deleteResource(50L, adminPrincipal);

        verify(fileStorageService, times(1)).deleteFile("uploads/algo.pdf");
        verify(resourceRepository, times(1)).delete(pendingResource);
        verify(auditLogRepository, times(1)).save(any(AdminAuditLog.class));
    }

    @Test
    @DisplayName("updateUserStatus should enable/disable other user accounts successfully")
    void testUpdateUserStatus_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(studentUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = adminService.updateUserStatus(2L, false, adminPrincipal);

        assertNotNull(response);
        assertFalse(response.isEnabled());
        verify(userRepository, times(1)).save(studentUser);
        verify(auditLogRepository, times(1)).save(any(AdminAuditLog.class));
    }

    @Test
    @DisplayName("updateUserStatus should throw IllegalArgumentException when admin attempts self-disable")
    void testUpdateUserStatus_SelfDisable_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                adminService.updateUserStatus(1L, false, adminPrincipal)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(auditLogRepository, never()).save(any(AdminAuditLog.class));
    }

    @Test
    @DisplayName("getUsers should return paginated UserResponses")
    void testGetUsers_Paginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(adminUser, studentUser), pageable, 2);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserResponse> result = adminService.getUsers(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Admin User", result.getContent().get(0).getFullName());
    }
}
