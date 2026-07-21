package com.urlshortener.repository;

import com.urlshortener.entity.ShortUrl;
import com.urlshortener.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository interface for {@link ShortUrl} entities.
 * Handles database operations for shortened URLs.
 */
@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    /**
     * Retrieves a short URL record by its unique short code slug.
     *
     * @param shortCode the unique identifier code
     * @return an {@link Optional} containing the ShortUrl if found
     */
    Optional<ShortUrl> findByShortCode(String shortCode);

    /**
     * Retrieves all short URLs owned by a specific user, sorted by creation date descending.
     *
     * @param user the owner of the short URLs
     * @return a list of ShortUrls
     */
    List<ShortUrl> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Checks if a short code is already present in the database.
     *
     * @param shortCode the unique code slug to check
     * @return true if the short code is already in use, false otherwise
     */
    boolean existsByShortCode(String shortCode);

    /**
     * Counts the total number of short URLs created by a specific user.
     *
     * @param user the user owner
     * @return the count of URLs created by the user
     */
    long countByUser(User user);

    /**
     * Aggregates the sum of all clicks across all short URLs owned by a specific user.
     * Uses COALESCE to return 0 if the user has not generated any clicks yet.
     *
     * @param user the user owner
     * @return the total click sum for the user
     */
    @Query("SELECT COALESCE(SUM(s.clickCount), 0) FROM ShortUrl s WHERE s.user = :user")
    int sumClicksByUser(@Param("user") User user);
}
