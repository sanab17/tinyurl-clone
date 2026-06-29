package com.urlshortener.repository;

import com.urlshortener.entity.ClickAnalytic;
import com.urlshortener.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClickAnalyticRepository extends JpaRepository<ClickAnalytic, Long> {
    List<ClickAnalytic> findByShortUrlOrderByClickTimeDesc(ShortUrl shortUrl);
    List<ClickAnalytic> findByShortUrl(ShortUrl shortUrl);
}
