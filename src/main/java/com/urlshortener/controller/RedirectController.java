package com.urlshortener.controller;

import com.urlshortener.entity.ShortUrl;
import com.urlshortener.service.ShortUrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class RedirectController {

    private final ShortUrlService shortUrlService;

    public RedirectController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @GetMapping("/{code:[a-zA-Z0-9_-]{3,20}}")
    public String redirect(@PathVariable("code") String code, HttpServletRequest request) {
        ShortUrl shortUrl = shortUrlService.getByShortCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short URL not found"));

        // Retrieve visitor details
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isBlank() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // In case there is a list of IPs, get the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");

        // Record the click analytic
        shortUrlService.recordClick(shortUrl, ipAddress, userAgent, referrer);

        // Redirect to original URL
        return "redirect:" + shortUrl.getOriginalUrl();
    }
}
