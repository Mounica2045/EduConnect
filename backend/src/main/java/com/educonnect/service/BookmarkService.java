package com.educonnect.service;

import com.educonnect.dto.BookmarkResponse;
import com.educonnect.dto.BookmarkStatusResponse;
import com.educonnect.dto.ResourcePageResponse;
import com.educonnect.dto.ResourceResponse;
import com.educonnect.entity.Bookmark;
import com.educonnect.entity.Resource;
import com.educonnect.entity.User;
import com.educonnect.enums.ResourceStatus;
import com.educonnect.exception.BookmarkNotFoundException;
import com.educonnect.exception.DuplicateBookmarkException;
import com.educonnect.exception.ResourceNotFoundException;
import com.educonnect.exception.UnauthorizedResourceAccessException;
import com.educonnect.repository.BookmarkRepository;
import com.educonnect.repository.ResourceRepository;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing student resource bookmarking, retrieval, and status checks.
 */
@Service
public class BookmarkService {

    private static final Logger logger = LoggerFactory.getLogger(BookmarkService.class);

    private final BookmarkRepository bookmarkRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public BookmarkService(
            BookmarkRepository bookmarkRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository
    ) {
        this.bookmarkRepository = bookmarkRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    /**
     * Bookmarks an approved educational resource for the authenticated student.
     */
    @Transactional
    public BookmarkResponse addBookmark(Long resourceId, UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedResourceAccessException("Authentication required to bookmark resources.");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + principal.getId()));

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(resourceId));

        if (resource.getStatus() != ResourceStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved study materials can be saved to bookmarks.");
        }

        if (bookmarkRepository.existsByUserIdAndResourceId(user.getId(), resourceId)) {
            throw new DuplicateBookmarkException(resourceId);
        }

        Bookmark bookmark = new Bookmark(user, resource);
        Bookmark saved = bookmarkRepository.save(bookmark);

        logger.info("Student id={} bookmarked resource id={}", user.getId(), resourceId);
        return BookmarkResponse.fromEntity(saved);
    }

    /**
     * Removes a bookmark for the authenticated student.
     */
    @Transactional
    public void removeBookmark(Long resourceId, UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedResourceAccessException("Authentication required to remove bookmarks.");
        }

        Bookmark bookmark = bookmarkRepository.findByUserIdAndResourceId(principal.getId(), resourceId)
                .orElseThrow(() -> new BookmarkNotFoundException(resourceId));

        bookmarkRepository.delete(bookmark);
        logger.info("Student id={} removed bookmark for resource id={}", principal.getId(), resourceId);
    }

    /**
     * Checks if a resource is bookmarked by the authenticated user.
     */
    @Transactional(readOnly = true)
    public BookmarkStatusResponse checkBookmarkStatus(Long resourceId, UserPrincipal principal) {
        if (principal == null) {
            return new BookmarkStatusResponse(resourceId, false);
        }
        boolean exists = bookmarkRepository.existsByUserIdAndResourceId(principal.getId(), resourceId);
        return new BookmarkStatusResponse(resourceId, exists);
    }

    /**
     * Retrieves paginated bookmarks for the authenticated student.
     */
    @Transactional(readOnly = true)
    public ResourcePageResponse getMyBookmarksPaginated(UserPrincipal principal, int page, int size) {
        if (principal == null) {
            throw new UnauthorizedResourceAccessException("Authentication required to view bookmarks.");
        }

        int safePage = Math.max(0, page);
        int safeSize = (size <= 0 || size > 100) ? 10 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Bookmark> bookmarkPage = bookmarkRepository.findByUserIdAndResourceStatus(
                principal.getId(), ResourceStatus.APPROVED, pageable
        );

        List<ResourceResponse> resourceList = bookmarkPage.getContent().stream()
                .map(b -> ResourceResponse.fromEntity(b.getResource()))
                .collect(Collectors.toList());

        return new ResourcePageResponse(
                resourceList,
                bookmarkPage.getNumber(),
                bookmarkPage.getSize(),
                bookmarkPage.getTotalElements(),
                bookmarkPage.getTotalPages(),
                bookmarkPage.isFirst(),
                bookmarkPage.isLast()
        );
    }

    /**
     * Retrieves list of approved bookmarked resources for the frontend.
     */
    @Transactional(readOnly = true)
    public List<ResourceResponse> getMyBookmarksList(UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedResourceAccessException("Authentication required to view bookmarks.");
        }

        List<Bookmark> bookmarks = bookmarkRepository.findByUserIdAndResourceStatusOrderByCreatedAtDesc(
                principal.getId(), ResourceStatus.APPROVED
        );

        return bookmarks.stream()
                .map(b -> ResourceResponse.fromEntity(b.getResource()))
                .collect(Collectors.toList());
    }

    /**
     * Returns total count of bookmarks in the portal.
     */
    @Transactional(readOnly = true)
    public long getTotalBookmarksCount() {
        return bookmarkRepository.count();
    }
}
