package com.urlshortener.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler that catches exceptions thrown across the application.
 * Translates exceptions into standardized HTTP status codes and user-friendly error views.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link ResponseStatusException} instances thrown by controllers.
     * Selects appropriate HTML templates (404, 429, 400, 401, 403, 500) based on the HTTP status.
     *
     * @param ex       the caught exception
     * @param response the HTTP response to set the correct status code on
     * @param model    the MVC model context
     * @return the error page template name
     */
    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatusException(ResponseStatusException ex, HttpServletResponse response, Model model) {
        int statusCode = ex.getStatusCode().value();
        if (ex.getStatusCode().is4xxClientError()) {
            logger.warn("Client error occurred: {} - {}", statusCode, ex.getReason());
        } else {
            logger.error("Server error occurred: {} - {}", statusCode, ex.getReason(), ex);
        }

        response.setStatus(statusCode);
        model.addAttribute("status", statusCode);
        model.addAttribute("errorMessage", ex.getReason());

        if (statusCode == 404) {
            return "error/404";
        } else if (statusCode == 429) {
            return "error/429";
        } else if (statusCode == 400) {
            return "error/400";
        } else if (statusCode == 401) {
            return "error/401";
        } else if (statusCode == 403) {
            return "error/403";
        }
        return "error/500";
    }

    /**
     * Handles custom {@link SecurityException} thrown during unauthorized resource access.
     * Sets status to 403 Forbidden and returns the access denied view.
     *
     * @param ex       the security exception
     * @param response the HTTP response
     * @param model    the MVC model context
     * @return 403 error view path
     */
    @ExceptionHandler(SecurityException.class)
    public String handleSecurityException(SecurityException ex, HttpServletResponse response, Model model) {
        logger.warn("Security violation attempt: {}", ex.getMessage());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        model.addAttribute("status", HttpStatus.FORBIDDEN.value());
        model.addAttribute("errorMessage", "Access Denied: " + ex.getMessage());
        return "error/403";
    }

    /**
     * Handles Spring database errors ({@link DataAccessException}).
     * Logs the raw database trace to server logs and returns a generic 500 error to the client.
     *
     * @param ex       the database exception
     * @param response the HTTP response
     * @param model    the MVC model context
     * @return 500 error view path
     */
    @ExceptionHandler(DataAccessException.class)
    public String handleDatabaseException(DataAccessException ex, HttpServletResponse response, Model model) {
        logger.error("Database access failure: {}", ex.getMessage(), ex);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("errorMessage", "A database error occurred. Please try again later.");
        return "error/500";
    }

    /**
     * Handles missing static resources or controller mapping failures ({@link NoResourceFoundException}).
     *
     * @param ex       the missing resource exception
     * @param response the HTTP response
     * @param model    the MVC model context
     * @return 404 error view path
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundException(NoResourceFoundException ex, HttpServletResponse response, Model model) {
        logger.warn("Resource or endpoint not found: {}", ex.getMessage());
        response.setStatus(HttpStatus.NOT_FOUND.value());
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        model.addAttribute("errorMessage", "The requested resource could not be found: /" + ex.getResourcePath());
        return "error/404";
    }

    /**
     * Fallback generic exception handler for all other unhandled exceptions.
     *
     * @param ex       the unhandled exception
     * @param response the HTTP response
     * @param model    the MVC model context
     * @return 500 error view path
     * @throws Exception if rethrowing security exceptions fails
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, HttpServletResponse response, Model model) throws Exception {
        // Rethrow Spring Security exceptions to let Spring Security filters handle them
        if (ex instanceof org.springframework.security.access.AccessDeniedException ||
            ex instanceof org.springframework.security.core.AuthenticationException) {
            throw ex;
        }

        logger.error("Unhandled exception caught by global handler: {}", ex.getMessage(), ex);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
        return "error/500";
    }
}
