package com.urlshortener.service;

import com.urlshortener.entity.ClickAnalytic;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.entity.User;
import com.urlshortener.repository.ClickAnalyticRepository;
import com.urlshortener.repository.ShortUrlRepository;
import com.urlshortener.util.UserAgentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

/**
 * Service class implementing the core business logic of the URL Shortener.
 * Handles the creation, deletion, redirect resolution, QR code injection,
 * and visitor click analytic tracking.
 */
@Service
public class ShortUrlService {

    private static final Logger logger = LoggerFactory.getLogger(ShortUrlService.class);

    private final ShortUrlRepository shortUrlRepository;
    private final ClickAnalyticRepository clickAnalyticRepository;
    private final QrCodeService qrCodeService;

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();

    /**
     * Constructs the ShortUrlService with required repository and helper services.
     *
     * @param shortUrlRepository      data access for short URLs
     * @param clickAnalyticRepository data access for click metrics
     * @param qrCodeService           service to generate QR codes
     */
    public ShortUrlService(ShortUrlRepository shortUrlRepository, 
                           ClickAnalyticRepository clickAnalyticRepository, 
                           QrCodeService qrCodeService) {
        this.shortUrlRepository = shortUrlRepository;
        this.clickAnalyticRepository = clickAnalyticRepository;
        this.qrCodeService = qrCodeService;
    }

    /**
     * Retrieves all short URLs created by a specific user ordered by creation timestamp descending.
     *
     * @param user the owner user
     * @return a list of short URLs
     */
    public List<ShortUrl> getUrlsByUser(User user) {
        return shortUrlRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Retrieves a short URL by its unique short code.
     *
     * @param shortCode the code slug representing the link
     * @return an {@link Optional} containing the ShortUrl if found
     */
    public Optional<ShortUrl> getByShortCode(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode);
    }

    /**
     * Counts the total number of shortened links created by a given user.
     *
     * @param user the owner user
     * @return the count of shortened links
     */
    public long getUrlCountByUser(User user) {
        return shortUrlRepository.countByUser(user);
    }

    /**
     * Summarizes the total click hits across all shortened links created by a user.
     *
     * @param user the owner user
     * @return the total click count
     */
    public int getTotalClicksByUser(User user) {
        return shortUrlRepository.sumClicksByUser(user);
    }

    /**
     * Retrieves all click analytics records associated with a specific short URL.
     *
     * @param shortUrl the target short URL
     * @return list of click analytics logs
     */
    public List<ClickAnalytic> getClicksForUrl(ShortUrl shortUrl) {
        return clickAnalyticRepository.findByShortUrl(shortUrl);
    }

    /**
     * Retrieves click analytics records associated with a short URL sorted by newest first.
     *
     * @param shortUrl the target short URL
     * @return list of click logs ordered by click time descending
     */
    public List<ClickAnalytic> getLatestClicksForUrl(ShortUrl shortUrl) {
        return clickAnalyticRepository.findByShortUrlOrderByClickTimeDesc(shortUrl);
    }

    /**
     * Business transaction logic to create a new shortened URL.
     * Validates destination URL, checks custom alias constraints/reservations,
     * generates a random short code if alias is omitted, generates a QR code, and saves the entity.
     *
     * @param originalUrl the target redirection URL destination
     * @param customAlias optional user-defined short code slug (must match validation criteria)
     * @param title       optional user-defined name for the link
     * @param user        the owner user creating the link
     * @param baseUrl     the base deployment domain (used to construct the full QR code link)
     * @return the saved {@link ShortUrl} entity
     * @throws IllegalArgumentException if the URL is invalid or custom alias is occupied/reserved
     */
    @Transactional
    public ShortUrl createShortUrl(String originalUrl, String customAlias, String title, User user, String baseUrl) throws IllegalArgumentException {
        if (originalUrl == null || originalUrl.isBlank()) {
            logger.warn("Short URL creation failed: Original URL is empty for user: {}", user.getUsername());
            throw new IllegalArgumentException("Destination URL is required.");
        }
        
        // Clean URL to ensure it has scheme (http/https)
        originalUrl = originalUrl.trim();
        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            originalUrl = "https://" + originalUrl;
        }

        String shortCode;
        if (customAlias != null && !customAlias.isBlank()) {
            customAlias = customAlias.trim();
            // Validate alias characters and length (3-20 length, alphanumeric/dash/underscore)
            if (!customAlias.matches("^[a-zA-Z0-9_-]{3,20}$")) {
                logger.warn("Short URL creation failed: Custom alias '{}' does not match pattern, requested by user: {}", customAlias, user.getUsername());
                throw new IllegalArgumentException("Custom alias must be 3-20 characters long and contain only letters, numbers, underscores, or hyphens.");
            }
            if (shortUrlRepository.existsByShortCode(customAlias)) {
                logger.warn("Short URL creation failed: Custom alias '{}' is already in use, requested by user: {}", customAlias, user.getUsername());
                throw new IllegalArgumentException("Custom alias '" + customAlias + "' is already in use.");
            }
            if (isReservedKeyword(customAlias)) {
                logger.warn("Short URL creation failed: Custom alias '{}' is a reserved system keyword, requested by user: {}", customAlias, user.getUsername());
                throw new IllegalArgumentException("Custom alias '" + customAlias + "' is a reserved system keyword.");
            }
            shortCode = customAlias;
            logger.info("User '{}' requested custom alias: '{}' for original URL: '{}'", user.getUsername(), customAlias, originalUrl);
        } else {
            // Generate a random unique alphanumeric slug
            shortCode = generateUniqueShortCode();
        }

        ShortUrl shortUrl = new ShortUrl(originalUrl, shortCode, title, user);

        // Generate QR code for the short link
        String fullShortUrl = baseUrl + "/" + shortCode;
        String qrCode = qrCodeService.generateQrCodeBase64(fullShortUrl, 250, 250);
        shortUrl.setQrCodeBase64(qrCode);

        ShortUrl savedShortUrl = shortUrlRepository.save(shortUrl);
        logger.info("Successfully created short URL. Code: {}, Original URL: {}, Created by user: {}", shortCode, originalUrl, user.getUsername());
        return savedShortUrl;
    }

    /**
     * Business transaction logic to delete a shortened URL.
     * Verifies that the requesting user owns the link before performing cascades.
     *
     * @param id   the database primary key ID of the short URL
     * @param user the requesting user attempting deletion
     * @throws IllegalArgumentException if the URL is not found
     * @throws SecurityException        if the user is not the owner of the short URL
     */
    @Transactional
    public void deleteShortUrl(Long id, User user) {
        ShortUrl shortUrl = shortUrlRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Short URL deletion failed: URL with ID {} not found, requested by user: {}", id, user.getUsername());
                    return new IllegalArgumentException("URL not found");
                });
        
        // Ownership verification check
        if (!shortUrl.getUser().getId().equals(user.getId())) {
            logger.warn("Unauthorized short URL deletion attempt: User '{}' tried to delete URL with ID {} owned by User '{}'", 
                    user.getUsername(), id, shortUrl.getUser().getUsername());
            throw new SecurityException("Unauthorized to delete this URL");
        }

        // Delete associated analytics first
        List<ClickAnalytic> analytics = clickAnalyticRepository.findByShortUrl(shortUrl);
        clickAnalyticRepository.deleteAll(analytics);
        
        // Delete URL
        shortUrlRepository.delete(shortUrl);
        logger.info("Successfully deleted short URL. Code: {}, ID: {}, Deleted by user: {}", shortUrl.getShortCode(), id, user.getUsername());
    }

    /**
     * Increments click counter metrics and logs visitor attributes to the analytics database tables.
     * Parses client OS and browser information from the raw user-agent string.
     *
     * @param shortUrl  the short URL redirection target
     * @param ipAddress visitor client IP address
     * @param userAgent visitor raw user-agent header
     * @param referrer  visitor HTTP referrer header
     */
    @Transactional
    public void recordClick(ShortUrl shortUrl, String ipAddress, String userAgent, String referrer) {
        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        shortUrlRepository.save(shortUrl);

        String browser = UserAgentParser.parseBrowser(userAgent);
        String os = UserAgentParser.parseOS(userAgent);

        String referrerDomain = "Direct / Email / SMS";
        if (referrer != null && !referrer.isBlank()) {
            try {
                java.net.URI uri = new java.net.URI(referrer);
                String host = uri.getHost();
                if (host != null) {
                    // Extract domain without 'www.' prefix
                    referrerDomain = host.startsWith("www.") ? host.substring(4) : host;
                } else {
                    referrerDomain = referrer;
                }
            } catch (Exception e) {
                referrerDomain = referrer;
            }
        }

        ClickAnalytic clickAnalytic = new ClickAnalytic(shortUrl, ipAddress, userAgent, browser, os, referrerDomain);
        clickAnalyticRepository.save(clickAnalytic);
    }

    /**
     * Loops generation of random 6-character alphanumeric keys until finding one that is unoccupied.
     *
     * @return a unique 6-character short code slug
     */
    private String generateUniqueShortCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            }
            code = sb.toString();
        } while (shortUrlRepository.existsByShortCode(code));
        return code;
    }

    /**
     * Checks custom slugs against system reserved words to prevent URL conflicts.
     *
     * @param word the user requested slug
     * @return true if reserved by the system, false otherwise
     */
    private boolean isReservedKeyword(String word) {
        String lower = word.toLowerCase();
        return lower.equals("login") || lower.equals("register") || lower.equals("dashboard") 
                || lower.equals("css") || lower.equals("js") || lower.equals("error")
                || lower.equals("logout") || lower.equals("h2-console");
    }
}
