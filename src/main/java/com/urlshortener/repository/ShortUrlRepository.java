package com.urlshortener.repository;

import com.urlshortener.entity.ShortUrl;
import com.urlshortener.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByShortCode(String shortCode);
    List<ShortUrl> findByUserOrderByCreatedAtDesc(User user);
    boolean existsByShortCode(String shortCode);
    long countByUser(User user);

    @Query("SELECT COALESCE(SUM(s.clickCount), 0) FROM ShortUrl s WHERE s.user = :user")
    int sumClicksByUser(@Param("user") User user);
}
