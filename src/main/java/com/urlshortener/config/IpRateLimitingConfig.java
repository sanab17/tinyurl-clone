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

@Configuration
public class IpRateLimitingConfig {

    @Bean
    public LoadingCache<String, Bucket> ipBucketCache(
            @Value("${app.rate-limit.requests-per-minute:20}") int requestsPerMinute,
            @Value("${app.rate-limit.refill-duration:PT1M}") Duration refillDuration) {

        return Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(1))
                .maximumSize(10_000)
                .build(key -> createBucket(requestsPerMinute, refillDuration));
    }

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
