package com.urlshortener.repository;

import com.urlshortener.entity.ClickAnalytic;
import com.urlshortener.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository interface for {@link ClickAnalytic} entities.
 * Handles database operations for visitor click logs.
 */
@Repository
public interface ClickAnalyticRepository extends JpaRepository<ClickAnalytic, Long> {

    /**
     * Finds all click logs associated with a specific short URL, ordered by click time descending.
     *
     * @param shortUrl the target shortened URL entity
     * @return a list of click records sorted by newest first
     */
    List<ClickAnalytic> findByShortUrlOrderByClickTimeDesc(ShortUrl shortUrl);

    /**
     * Finds all click logs associated with a specific short URL.
     *
     * @param shortUrl the target shortened URL entity
     * @return a list of click records
     */
    List<ClickAnalytic> findByShortUrl(ShortUrl shortUrl);
}
