package com.educonnect.service;

import com.educonnect.dto.UpdateProfileRequest;
import com.educonnect.dto.UserResponse;
import com.educonnect.entity.User;
import com.educonnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user profile updates and administrative user records.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Updates an authenticated user's profile details.
     */
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found with ID: " + userId));

        user.setFullName(request.getFullName());
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getSemester() != null) {
            user.setSemester(request.getSemester());
        }

        User updatedUser = userRepository.save(user);
        logger.info("User {} updated profile details successfully", user.getEmail());

        return UserResponse.from(updatedUser);
    }
}
