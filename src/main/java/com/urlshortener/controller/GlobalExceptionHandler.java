package com.urlshortener.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatusException(ResponseStatusException ex, RedirectAttributes redirectAttributes) {
        if (ex.getStatusCode().is4xxClientError()) {
            logger.warn("Client error occurred: {} - {}", ex.getStatusCode(), ex.getReason());
        } else {
            logger.error("Server error occurred: {} - {}", ex.getStatusCode(), ex.getReason(), ex);
        }
        redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        return "redirect:/dashboard";
    }

    @ExceptionHandler(SecurityException.class)
    public String handleSecurityException(SecurityException ex, RedirectAttributes redirectAttributes) {
        logger.warn("Security violation attempt: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/dashboard";
    }

    @ExceptionHandler(DataAccessException.class)
    public String handleDatabaseException(DataAccessException ex, RedirectAttributes redirectAttributes) {
        logger.error("Database access failure: {}", ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute("errorMessage", "Database error occurred. Please try again later.");
        return "redirect:/dashboard";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, RedirectAttributes redirectAttributes) {
        logger.error("Unhandled exception caught by global handler: {}", ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred.");
        return "redirect:/dashboard";
    }
}
