package com.urlshortener.service;

import com.urlshortener.entity.ClickAnalytic;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.entity.User;
import com.urlshortener.repository.ClickAnalyticRepository;
import com.urlshortener.repository.ShortUrlRepository;
import com.urlshortener.util.UserAgentParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final ClickAnalyticRepository clickAnalyticRepository;
    private final QrCodeService qrCodeService;

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();

    public ShortUrlService(ShortUrlRepository shortUrlRepository, 
                           ClickAnalyticRepository clickAnalyticRepository, 
                           QrCodeService qrCodeService) {
        this.shortUrlRepository = shortUrlRepository;
        this.clickAnalyticRepository = clickAnalyticRepository;
        this.qrCodeService = qrCodeService;
    }

    public List<ShortUrl> getUrlsByUser(User user) {
        return shortUrlRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Optional<ShortUrl> getByShortCode(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode);
    }

    public long getUrlCountByUser(User user) {
        return shortUrlRepository.countByUser(user);
    }

    public int getTotalClicksByUser(User user) {
        return shortUrlRepository.sumClicksByUser(user);
    }

    public List<ClickAnalytic> getClicksForUrl(ShortUrl shortUrl) {
        return clickAnalyticRepository.findByShortUrl(shortUrl);
    }

    public List<ClickAnalytic> getLatestClicksForUrl(ShortUrl shortUrl) {
        return clickAnalyticRepository.findByShortUrlOrderByClickTimeDesc(shortUrl);
    }

    @Transactional
    public ShortUrl createShortUrl(String originalUrl, String customAlias, String title, User user, String baseUrl) throws IllegalArgumentException {
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new IllegalArgumentException("Original URL cannot be empty");
        }
        
        // Clean URL to ensure it has scheme (http/https)
        originalUrl = originalUrl.trim();
        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            originalUrl = "https://" + originalUrl;
        }

        String shortCode;
        if (customAlias != null && !customAlias.isBlank()) {
            customAlias = customAlias.trim();
            if (!customAlias.matches("^[a-zA-Z0-9_-]{3,20}$")) {
                throw new IllegalArgumentException("Custom alias must be 3-20 characters long and contain only letters, numbers, underscores, or hyphens");
            }
            if (shortUrlRepository.existsByShortCode(customAlias)) {
                throw new IllegalArgumentException("Custom alias '" + customAlias + "' is already in use");
            }
            if (isReservedKeyword(customAlias)) {
                throw new IllegalArgumentException("Custom alias '" + customAlias + "' is a reserved system keyword");
            }
            shortCode = customAlias;
        } else {
            shortCode = generateUniqueShortCode();
        }

        ShortUrl shortUrl = new ShortUrl(originalUrl, shortCode, title, user);

        // Generate QR code for the short link
        String fullShortUrl = baseUrl + "/" + shortCode;
        String qrCode = qrCodeService.generateQrCodeBase64(fullShortUrl, 250, 250);
        shortUrl.setQrCodeBase64(qrCode);

        return shortUrlRepository.save(shortUrl);
    }

    @Transactional
    public void deleteShortUrl(Long id, User user) {
        ShortUrl shortUrl = shortUrlRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("URL not found"));
        
        if (!shortUrl.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized to delete this URL");
        }

        // Delete associated analytics first
        List<ClickAnalytic> analytics = clickAnalyticRepository.findByShortUrl(shortUrl);
        clickAnalyticRepository.deleteAll(analytics);
        
        // Delete URL
        shortUrlRepository.delete(shortUrl);
    }

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

    private boolean isReservedKeyword(String word) {
        String lower = word.toLowerCase();
        return lower.equals("login") || lower.equals("register") || lower.equals("dashboard") 
                || lower.equals("css") || lower.equals("js") || lower.equals("error")
                || lower.equals("logout") || lower.equals("h2-console");
    }
}
