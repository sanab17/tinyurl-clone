package com.urlshortener.config;

import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IpRateLimitFilterTest {

    private LoadingCache<String, Bucket> bucketCache;
    private IpRateLimitFilter filter;
    private final String endpoint = "/dashboard/create";
    private final Duration refillDuration = Duration.ofMinutes(1);

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        bucketCache = mock(LoadingCache.class);
        filter = new IpRateLimitFilter(bucketCache, endpoint, refillDuration);
    }

    @Test
    void testShouldNotFilter_NonPostRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getServletPath()).thenReturn(endpoint);

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void testShouldNotFilter_NonTargetEndpoint() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/dashboard");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void testShouldFilter_PostToTargetEndpoint() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn(endpoint);

        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void testDoFilterInternal_AllowedRequest() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        Bucket bucket = mock(Bucket.class);

        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn(endpoint);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(bucketCache.get("192.168.1.1")).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testDoFilterInternal_RateLimitedRequest() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        Bucket bucket = mock(Bucket.class);

        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn(endpoint);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(bucketCache.get("192.168.1.1")).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(false);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        verify(response, times(1)).setStatus(429);
        verify(response, times(1)).setHeader("Retry-After", "60");
        verify(response, times(1)).setContentType("application/json;charset=UTF-8");
        writer.flush();
        assertTrue(stringWriter.toString().contains("Too many requests"));
    }

    @Test
    void testIpExtraction_XForwardedFor() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        Bucket bucket = mock(Bucket.class);

        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn(endpoint);
        when(request.getHeader("X-Forwarded-For")).thenReturn(" 203.0.113.195, 70.41.3.18, 150.172.238.178");
        when(bucketCache.get("203.0.113.195")).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(bucketCache, times(1)).get("203.0.113.195");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testIpExtraction_XForwardedFor_BlankHeader() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        Bucket bucket = mock(Bucket.class);

        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn(endpoint);
        when(request.getHeader("X-Forwarded-For")).thenReturn("   ");
        when(request.getRemoteAddr()).thenReturn("192.168.1.2");
        when(bucketCache.get("192.168.1.2")).thenReturn(bucket);
        when(bucket.tryConsume(1)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(bucketCache, times(1)).get("192.168.1.2");
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
