package com.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point class for the Spring Boot URL Shortener application.
 * Bootstraps the application, runs auto-configuration, and starts the embedded tomcat server.
 */
@SpringBootApplication
public class UrlShortenerApplication {

    /**
     * Main execution entry point invoked by the JVM.
     * Launches the Spring application context.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApplication.class, args);
    }
}
