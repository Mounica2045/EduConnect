package com.educonnect.repository;

import com.educonnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by normalized email.
     *
     * @param email normalized lowercase email
     * @return Optional containing the User if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user already exists with the given email.
     *
     * @param email normalized lowercase email
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);
}
