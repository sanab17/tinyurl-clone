package com.urlshortener.controller;

import com.urlshortener.entity.ShortUrl;
import com.urlshortener.service.ShortUrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller responsible for redirecting short code request URLs to their destination URLs.
 * Also parses client info (IP, User-Agent, Referrer) and records click logs.
 */
@Controller
public class RedirectController {

    private static final Logger logger = LoggerFactory.getLogger(RedirectController.class);

    private final ShortUrlService shortUrlService;

    /**
     * Constructs the redirect controller.
     *
     * @param shortUrlService the short URL service containing business logic for redirect/analytics
     */
    public RedirectController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    /**
     * Redirects short code URLs to their original URLs.
     * Extracts visitor details (IP address, User-Agent, Referrer) to record click analytics,
     * and performs a HTTP redirect on success. Throws NOT_FOUND if code doesn't exist.
     *
     * @param code    the short URL code path parameter
     * @param request the HTTP servlet request
     * @return redirect string target
     * @throws ResponseStatusException HTTP 404 if the short code is not found
     */
    @GetMapping("/{code:[a-zA-Z0-9_-]{3,20}}")
    public String redirect(@PathVariable("code") String code, HttpServletRequest request) {
        // Retrieve visitor details
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isBlank() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // In case there is a list of IPs, get the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        final String clientIp = ipAddress;

        ShortUrl shortUrl = shortUrlService.getByShortCode(code)
                .orElseThrow(() -> {
                    logger.warn("Redirect failed: Short code '{}' not found. Client IP: {}, Request URI: {}", 
                            code, clientIp, request.getRequestURI());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Short URL not found");
                });

        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");

        // Record the click analytic
        shortUrlService.recordClick(shortUrl, clientIp, userAgent, referrer);

        logger.info("Successfully redirected short code '{}' to original URL '{}'. Client IP: {}", 
                code, shortUrl.getOriginalUrl(), clientIp);

        // Redirect to original URL
        return "redirect:" + shortUrl.getOriginalUrl();
    }
}
