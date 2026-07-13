package com.urlshortener.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventsLogger {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationEventsLogger.class);

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        String username = success.getAuthentication().getName();
        logger.info("Successful login for user: {}", username);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failures) {
        String username = failures.getAuthentication().getName();
        logger.warn("Unsuccessful login attempt for user: {}. Reason: {}", username, failures.getException().getMessage());
    }
}
