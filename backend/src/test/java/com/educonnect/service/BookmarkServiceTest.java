package com.educonnect.service;

import com.educonnect.dto.BookmarkResponse;
import com.educonnect.dto.BookmarkStatusResponse;
import com.educonnect.dto.ResourcePageResponse;
import com.educonnect.dto.ResourceResponse;
import com.educonnect.entity.Bookmark;
import com.educonnect.entity.Resource;
import com.educonnect.entity.User;
import com.educonnect.enums.ResourceCategory;
import com.educonnect.enums.ResourceStatus;
import com.educonnect.enums.Role;
import com.educonnect.exception.BookmarkNotFoundException;
import com.educonnect.exception.DuplicateBookmarkException;
import com.educonnect.exception.ResourceNotFoundException;
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
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookmarkService bookmarkService;

    private User student;
    private UserPrincipal studentPrincipal;
    private Resource approvedResource;
    private Resource pendingResource;
    private Bookmark testBookmark;

    @BeforeEach
    void setUp() {
        student = new User("Aarav Sharma", "student@college.edu", "hash123", "Computer Science", "Semester 6", Role.STUDENT);
        student.setId(10L);
        studentPrincipal = UserPrincipal.create(student);

        approvedResource = new Resource();
        approvedResource.setId(100L);
        approvedResource.setTitle("DSA Notes");
        approvedResource.setStatus(ResourceStatus.APPROVED);
        approvedResource.setCategory(ResourceCategory.NOTES);

        pendingResource = new Resource();
        pendingResource.setId(200L);
        pendingResource.setTitle("Draft Syllabus");
        pendingResource.setStatus(ResourceStatus.PENDING);
        pendingResource.setCategory(ResourceCategory.OTHER);

        testBookmark = new Bookmark(student, approvedResource);
        testBookmark.setId(1L);
    }

    @Test
    @DisplayName("addBookmark should succeed for approved resource")
    void testAddBookmark_Success() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(resourceRepository.findById(100L)).thenReturn(Optional.of(approvedResource));
        when(bookmarkRepository.existsByUserIdAndResourceId(10L, 100L)).thenReturn(false);
        when(bookmarkRepository.save(any(Bookmark.class))).thenAnswer(inv -> {
            Bookmark b = inv.getArgument(0);
            b.setId(50L);
            return b;
        });

        BookmarkResponse response = bookmarkService.addBookmark(100L, studentPrincipal);

        assertNotNull(response);
        assertEquals(50L, response.getId());
        assertEquals(100L, response.getResourceId());
        verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
    }

    @Test
    @DisplayName("addBookmark should throw IllegalArgumentException for unapproved resource")
    void testAddBookmark_UnapprovedResource_ThrowsException() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(resourceRepository.findById(200L)).thenReturn(Optional.of(pendingResource));

        assertThrows(IllegalArgumentException.class, () ->
                bookmarkService.addBookmark(200L, studentPrincipal)
        );

        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    @Test
    @DisplayName("addBookmark should throw DuplicateBookmarkException on duplicate save")
    void testAddBookmark_Duplicate_ThrowsException() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(resourceRepository.findById(100L)).thenReturn(Optional.of(approvedResource));
        when(bookmarkRepository.existsByUserIdAndResourceId(10L, 100L)).thenReturn(true);

        assertThrows(DuplicateBookmarkException.class, () ->
                bookmarkService.addBookmark(100L, studentPrincipal)
        );

        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    @Test
    @DisplayName("removeBookmark should succeed when bookmark exists")
    void testRemoveBookmark_Success() {
        when(bookmarkRepository.findByUserIdAndResourceId(10L, 100L)).thenReturn(Optional.of(testBookmark));

        bookmarkService.removeBookmark(100L, studentPrincipal);

        verify(bookmarkRepository, times(1)).delete(testBookmark);
    }

    @Test
    @DisplayName("removeBookmark should throw BookmarkNotFoundException when not found")
    void testRemoveBookmark_NotFound_ThrowsException() {
        when(bookmarkRepository.findByUserIdAndResourceId(10L, 999L)).thenReturn(Optional.empty());

        assertThrows(BookmarkNotFoundException.class, () ->
                bookmarkService.removeBookmark(999L, studentPrincipal)
        );
    }

    @Test
    @DisplayName("checkBookmarkStatus should return true when bookmarked")
    void testCheckBookmarkStatus_True() {
        when(bookmarkRepository.existsByUserIdAndResourceId(10L, 100L)).thenReturn(true);

        BookmarkStatusResponse status = bookmarkService.checkBookmarkStatus(100L, studentPrincipal);

        assertNotNull(status);
        assertEquals(100L, status.getResourceId());
        assertTrue(status.isBookmarked());
    }

    @Test
    @DisplayName("getMyBookmarksList should return only approved bookmarked resources")
    void testGetMyBookmarksList() {
        when(bookmarkRepository.findByUserIdAndResourceStatusOrderByCreatedAtDesc(10L, ResourceStatus.APPROVED))
                .thenReturn(List.of(testBookmark));

        List<ResourceResponse> list = bookmarkService.getMyBookmarksList(studentPrincipal);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(100L, list.get(0).getId());
    }

    @Test
    @DisplayName("getMyBookmarksPaginated should return paginated resources")
    void testGetMyBookmarksPaginated() {
        Page<Bookmark> page = new PageImpl<>(List.of(testBookmark));
        when(bookmarkRepository.findByUserIdAndResourceStatus(eq(10L), eq(ResourceStatus.APPROVED), any(Pageable.class)))
                .thenReturn(page);

        ResourcePageResponse pageResponse = bookmarkService.getMyBookmarksPaginated(studentPrincipal, 0, 10);

        assertNotNull(pageResponse);
        assertEquals(1, pageResponse.getContent().size());
        assertEquals(100L, pageResponse.getContent().get(0).getId());
    }
}
