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
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;
import java.time.Duration;

/**
 * Servlet filter that enforces IP-based rate limiting on specific POST endpoints.
 * It uses Bucket4j for the token-bucket algorithm and Caffeine for cache eviction.
 */
public class IpRateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(IpRateLimitFilter.class);

    private final LoadingCache<String, Bucket> bucketCache;
    private final String rateLimitEndpoint;
    private final Duration retryAfter;

    /**
     * Constructs a new IP rate limiting filter.
     *
     * @param bucketCache        the Caffeine cache holding the rate-limiting buckets per IP
     * @param rateLimitEndpoint the path pattern that should be rate-limited (e.g., "/dashboard/shorten")
     * @param retryAfter        the duration for clients to wait before retrying when rate-limited
     */
    public IpRateLimitFilter(LoadingCache<String, Bucket> bucketCache,
                             String rateLimitEndpoint,
                             Duration retryAfter) {
        this.bucketCache = bucketCache;
        this.rateLimitEndpoint = rateLimitEndpoint;
        this.retryAfter = retryAfter;
    }

    /**
     * Determines whether to skip rate limit filtering for the incoming request.
     * We only rate limit POST requests that target the specified rate limit endpoint.
     *
     * @param request the HTTP servlet request
     * @return true if the request should bypass this filter, false otherwise
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod())
                || !request.getServletPath().equals(rateLimitEndpoint);
    }

    /**
     * Enforces the rate limit constraint. If a token can be consumed from the IP's bucket,
     * the request proceeds. Otherwise, a 429 Too Many Requests response is returned (or redirected with flash error).
     *
     * @param request     the HTTP servlet request
     * @param response    the HTTP servlet response
     * @param filterChain the servlet filter chain
     * @throws ServletException if any servlet level exception occurs
     * @throws IOException      if any input/output exception occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = extractClientIp(request);
        Bucket bucket = bucketCache.get(clientIp);

        // Attempt to consume 1 token from the IP's bucket
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        logger.warn("Rate limit exceeded for client IP: {} on endpoint: {}", clientIp, request.getServletPath());

        String acceptHeader = request.getHeader("Accept");
        // For browser users requesting HTML, redirect them to dashboard with a flash message
        if (acceptHeader != null && acceptHeader.contains("text/html")) {
            FlashMap flashMap = new FlashMap();
            flashMap.put("errorMessage", "You are doing that too fast. Please wait a moment before trying again.");
            
            // Instantiated directly to bypass the uninitialized DispatcherServlet attributes
            FlashMapManager flashMapManager = new SessionFlashMapManager();
            flashMapManager.saveOutputFlashMap(flashMap, request, response);
            
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        // For non-HTML/API client requests, return a JSON 429 error response
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfter.toSeconds()));
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"Too many requests. Try again later.\"}");
    }

    /**
     * Helper method to extract the client IP address from the request header (X-Forwarded-For)
     * or fallback to remote address if no proxy header exists.
     *
     * @param request the HTTP request
     * @return the extracted client IP address
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // In case of multiple proxies, take the first client IP in the chain
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
