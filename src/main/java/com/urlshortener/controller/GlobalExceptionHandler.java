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

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(SecurityException.class)
    public String handleSecurityException(SecurityException ex, HttpServletResponse response, Model model) {
        logger.warn("Security violation attempt: {}", ex.getMessage());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        model.addAttribute("status", HttpStatus.FORBIDDEN.value());
        model.addAttribute("errorMessage", "Access Denied: " + ex.getMessage());
        return "error/403";
    }

    @ExceptionHandler(DataAccessException.class)
    public String handleDatabaseException(DataAccessException ex, HttpServletResponse response, Model model) {
        logger.error("Database access failure: {}", ex.getMessage(), ex);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("errorMessage", "A database error occurred. Please try again later.");
        return "error/500";
    }

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
