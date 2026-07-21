package com.urlshortener.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Database entity representing a shortened URL.
 * Maps to the "short_urls" database table and maintains metadata such as clicks and generated QR codes.
 */
@Entity
@Table(name = "short_urls")
public class ShortUrl {

    /**
     * Unique identifier for the short URL record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The full original destination URL that visitors are redirected to.
     */
    @Column(nullable = false, length = 2048)
    private String originalUrl;

    /**
     * Unique alphanumeric key/slug (e.g. "aB3d9f") representing the short link.
     */
    @Column(unique = true, nullable = false, length = 100)
    private String shortCode;

    /**
     * User-defined title/label for identification on the dashboard.
     */
    @Column(length = 255)
    private String title;

    /**
     * Timestamp indicating when this short link was created.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Counter of total redirection hits this link has received.
     */
    @Column(nullable = false)
    private int clickCount = 0;

    /**
     * The User owner who created this short URL.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Base64-encoded PNG image representation of the QR code for this link.
     */
    @Column(columnDefinition = "TEXT")
    private String qrCodeBase64;

    /**
     * Collection of individual click logs/events associated with this short link.
     */
    @OneToMany(mappedBy = "shortUrl", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClickAnalytic> clickAnalytics = new ArrayList<>();

    /**
     * Default constructor required by JPA.
     */
    public ShortUrl() {
    }

    /**
     * Constructs a new ShortUrl with basic attributes, defaulting the title if empty.
     *
     * @param originalUrl destination URL
     * @param shortCode   unique code slug
     * @param title       optional display title
     * @param user        link owner
     */
    public ShortUrl(String originalUrl, String shortCode, String title, User user) {
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.title = title != null && !title.isBlank() ? title : originalUrl;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.clickCount = 0;
    }

    /**
     * JPA lifecycle callback executed before persisting a new short URL.
     * Guarantees the creation date is set to the current system time.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getClickCount() {
        return clickCount;
    }

    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }

    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
    }

    public List<ClickAnalytic> getClickAnalytics() {
        return clickAnalytics;
    }

    public void setClickAnalytics(List<ClickAnalytic> clickAnalytics) {
        this.clickAnalytics = clickAnalytics;
    }
}
