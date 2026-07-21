package com.urlshortener.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Component that listens for Spring Security authentication events and logs the outcomes.
 * This helper class provides visibility into both successful and failed authentication attempts.
 */
@Component
public class AuthenticationEventsLogger {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationEventsLogger.class);

    /**
     * Event listener triggered upon successful user authentication.
     * Logs the username of the user who successfully logged in.
     *
     * @param success the authentication success event details
     */
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        String username = success.getAuthentication().getName();
        logger.info("Successful login for user: {}", username);
    }

    /**
     * Event listener triggered upon failed user authentication.
     * Logs the username attempting the login and the reason for the failure.
     *
     * @param failures the authentication failure event details
     */
    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failures) {
        String username = failures.getAuthentication().getName();
        logger.warn("Unsuccessful login attempt for user: {}. Reason: {}", username, failures.getException().getMessage());
    }
}
