package com.urlshortener.repository;

import com.urlshortener.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository interface for {@link User} entities.
 * Handles database operations for user account registration and details loading.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieves a user entity based on their unique username.
     *
     * @param username the username to look up
     * @return an {@link Optional} containing the User if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks if a user record already exists with the specified username.
     * Used for registration duplication checks.
     *
     * @param username the username to verify
     * @return true if the username is already taken, false otherwise
     */
    boolean existsByUsername(String username);
}
