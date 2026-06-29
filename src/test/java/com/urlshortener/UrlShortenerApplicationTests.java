package com.urlshortener;

import com.urlshortener.entity.ShortUrl;
import com.urlshortener.entity.User;
import com.urlshortener.repository.ShortUrlRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.service.ShortUrlService;
import com.urlshortener.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UrlShortenerApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private ShortUrlService shortUrlService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        shortUrlRepository.deleteAll();
        userRepository.deleteAll();
        testUser = userService.registerUser("testuser", "password123");
    }

    @Test
    void contextLoads() {
        assertNotNull(userService);
        assertNotNull(shortUrlService);
    }

    @Test
    void testRegisterUser() {
        assertNotNull(testUser.getId());
        assertEquals("testuser", testUser.getUsername());
        assertTrue(userRepository.existsByUsername("testuser"));
    }

    @Test
    void testRegisterDuplicateUserThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("testuser", "anotherpassword");
        });
    }

    @Test
    void testCreateShortUrlRandomCode() {
        ShortUrl shortUrl = shortUrlService.createShortUrl(
                "https://www.google.com", 
                null, 
                "Google", 
                testUser, 
                "http://localhost:8080"
        );

        assertNotNull(shortUrl);
        assertNotNull(shortUrl.getId());
        assertEquals(6, shortUrl.getShortCode().length());
        assertEquals("https://www.google.com", shortUrl.getOriginalUrl());
        assertEquals("Google", shortUrl.getTitle());
        assertEquals(testUser.getId(), shortUrl.getUser().getId());
    }

    @Test
    void testCreateShortUrlCustomAlias() {
        ShortUrl shortUrl = shortUrlService.createShortUrl(
                "https://github.com", 
                "mygithub", 
                "My GitHub", 
                testUser, 
                "http://localhost:8080"
        );

        assertNotNull(shortUrl);
        assertEquals("mygithub", shortUrl.getShortCode());
        assertEquals("https://github.com", shortUrl.getOriginalUrl());
    }

    @Test
    void testCreateShortUrlDuplicateCustomAliasThrowsException() {
        shortUrlService.createShortUrl(
                "https://github.com", 
                "mygithub", 
                "My GitHub", 
                testUser, 
                "http://localhost:8080"
        );

        assertThrows(IllegalArgumentException.class, () -> {
            shortUrlService.createShortUrl(
                    "https://gitlab.com", 
                    "mygithub", 
                    "My GitLab", 
                    testUser, 
                    "http://localhost:8080"
            );
        });
    }

    @Test
    void testCreateShortUrlInvalidCustomAliasThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            shortUrlService.createShortUrl(
                    "https://github.com", 
                    "ab", // Too short (min 3)
                    "Short", 
                    testUser, 
                    "http://localhost:8080"
            );
        });

        assertThrows(IllegalArgumentException.class, () -> {
            shortUrlService.createShortUrl(
                    "https://github.com", 
                    "invalid@alias", // Special chars
                    "Invalid Chars", 
                    testUser, 
                    "http://localhost:8080"
            );
        });
    }

    @Test
    void testRecordClick() {
        ShortUrl shortUrl = shortUrlService.createShortUrl(
                "https://news.ycombinator.com", 
                "hnews", 
                "Hacker News", 
                testUser, 
                "http://localhost:8080"
        );

        assertEquals(0, shortUrl.getClickCount());

        shortUrlService.recordClick(
                shortUrl, 
                "127.0.0.1", 
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36", 
                "https://t.co/somelink"
        );

        // Fetch again from DB to verify persistence of click count increment
        ShortUrl updatedUrl = shortUrlRepository.findById(shortUrl.getId()).orElseThrow();
        assertEquals(1, updatedUrl.getClickCount());

        var clicks = shortUrlService.getClicksForUrl(updatedUrl);
        assertEquals(1, clicks.size());
        
        var click = clicks.getFirst();
        assertEquals("127.0.0.1", click.getIpAddress());
        assertEquals("Chrome", click.getBrowser());
        assertEquals("macOS", click.getOperatingSystem());
        assertEquals("t.co", click.getReferrer());
    }
}
