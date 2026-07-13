package com.urlshortener.config;

import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

public class IpRateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(IpRateLimitFilter.class);

    private final LoadingCache<String, Bucket> bucketCache;
    private final String rateLimitEndpoint;
    private final Duration retryAfter;

    public IpRateLimitFilter(LoadingCache<String, Bucket> bucketCache,
                             String rateLimitEndpoint,
                             Duration retryAfter) {
        this.bucketCache = bucketCache;
        this.rateLimitEndpoint = rateLimitEndpoint;
        this.retryAfter = retryAfter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod())
                || !request.getServletPath().equals(rateLimitEndpoint);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = extractClientIp(request);
        Bucket bucket = bucketCache.get(clientIp);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        logger.warn("Rate limit exceeded for client IP: {} on endpoint: {}", clientIp, request.getServletPath());

        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfter.toSeconds()));
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"Too many requests. Try again later.\"}");
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
