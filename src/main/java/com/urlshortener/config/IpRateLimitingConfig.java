package com.urlshortener.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Collections;

/**
 * Configuration class that configures IP-based rate limiting using Bucket4j and Caffeine Cache.
 * Provides beans to instantiate the token buckets and register the rate limiting filter.
 */
@Configuration
public class IpRateLimitingConfig {

    /**
     * Configures a Caffeine Cache that stores rate-limiting buckets for each unique client IP.
     * Buckets expire after 1 hour of inactivity, and the cache has a maximum capacity of 10,000 IPs.
     *
     * @param requestsPerMinute the maximum number of requests allowed within the refill window
     * @param refillDuration    the duration of the refill window
     * @return a LoadingCache mapping IP strings to Bucket instances
     */
    @Bean
    public LoadingCache<String, Bucket> ipBucketCache(
            @Value("${app.rate-limit.requests-per-minute:20}") int requestsPerMinute,
            @Value("${app.rate-limit.refill-duration:PT1M}") Duration refillDuration) {

        return Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(1))
                .maximumSize(10_000)
                .build(key -> createBucket(requestsPerMinute, refillDuration));
    }

    /**
     * Registers the {@link IpRateLimitFilter} into the servlet filter chain.
     * Configures the rate-limiting endpoint paths and sets its execution order.
     *
     * @param ipBucketCache      the Caffeine cache that holds client buckets
     * @param rateLimitEndpoint  the specific endpoint path to rate limit
     * @param refillDuration     the refill duration used to configure the HTTP Retry-After header
     * @return the filter registration bean
     */
    @Bean
    public FilterRegistrationBean<IpRateLimitFilter> ipRateLimitFilter(
            LoadingCache<String, Bucket> ipBucketCache,
            @Value("${app.rate-limit.endpoint:/dashboard/create}") String rateLimitEndpoint,
            @Value("${app.rate-limit.refill-duration:PT1M}") Duration refillDuration) {

        IpRateLimitFilter filter = new IpRateLimitFilter(ipBucketCache, rateLimitEndpoint, refillDuration);

        FilterRegistrationBean<IpRateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setUrlPatterns(Collections.singletonList(rateLimitEndpoint));
        registration.setOrder(1);
        return registration;
    }

    /**
     * Helper method to create a new rate limit bucket.
     * Uses an interval-based refill where all capacity is restored after the refill duration.
     *
     * @param requestsPerMinute capacity limit of the bucket
     * @param refillDuration    how long it takes to refill the bucket completely
     * @return a configured {@link Bucket} instance
     */
    private static Bucket createBucket(int requestsPerMinute, Duration refillDuration) {
        Bandwidth limit = Bandwidth.classic(
                requestsPerMinute,
                Refill.intervally(requestsPerMinute, refillDuration)
        );

        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }
}
