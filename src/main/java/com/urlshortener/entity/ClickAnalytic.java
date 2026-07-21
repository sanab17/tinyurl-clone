package com.urlshortener.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Database entity representing click analytics data captured when a short URL is accessed.
 * Maps to the "click_analytics" database table.
 */
@Entity
@Table(name = "click_analytics")
public class ClickAnalytic {

    /**
     * Primary key of the analytic log entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The ShortUrl associated with this click event.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    private ShortUrl shortUrl;

    /**
     * Date and time when the redirection event occurred.
     */
    @Column(nullable = false)
    private LocalDateTime clickTime;

    /**
     * Client IP address of the visitor.
     */
    private String ipAddress;

    /**
     * Raw User-Agent string from the client's HTTP request header.
     */
    @Column(length = 1024)
    private String userAgent;

    /**
     * Parsed browser name (e.g., Chrome, Firefox, Safari).
     */
    private String browser;

    /**
     * Parsed client operating system (e.g., Windows, macOS, Linux, Android).
     */
    private String operatingSystem;

    /**
     * Referrer URL domain from the HTTP headers, indicating the origin source.
     */
    @Column(length = 1024)
    private String referrer;

    /**
     * Default constructor required by JPA.
     */
    public ClickAnalytic() {
    }

    /**
     * Constructs a new ClickAnalytic record with the current timestamp.
     *
     * @param shortUrl        associated short URL target
     * @param ipAddress       visitor client IP
     * @param userAgent       visitor raw user-agent string
     * @param browser         parsed browser name
     * @param operatingSystem parsed operating system name
     * @param referrer        HTTP referrer link source
     */
    public ClickAnalytic(ShortUrl shortUrl, String ipAddress, String userAgent, String browser, String operatingSystem, String referrer) {
        this.shortUrl = shortUrl;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.browser = browser;
        this.operatingSystem = operatingSystem;
        this.referrer = referrer;
        this.clickTime = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback executed before persisting a new record.
     * Guarantees clickTime is initialized with the current system time.
     */
    @PrePersist
    protected void onCreate() {
        if (this.clickTime == null) {
            this.clickTime = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ShortUrl getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(ShortUrl shortUrl) {
        this.shortUrl = shortUrl;
    }

    public LocalDateTime getClickTime() {
        return clickTime;
    }

    public void setClickTime(LocalDateTime clickTime) {
        this.clickTime = clickTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }
}
