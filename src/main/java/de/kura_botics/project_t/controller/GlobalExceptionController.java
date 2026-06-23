package de.kura_botics.project_t.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Provides global exception handling across all controllers in the application.
 * Centralizes error management, logging, and fallback routing.
 */
@ControllerAdvice
public class GlobalExceptionController {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionController.class);
    private static final String REDIRECT_HOME = "redirect:/";

    /**
     * Intercepts requests for missing resources or unknown endpoints (404 Not Found).
     * Instead of showing an error page, it redirects the user safely back to the home page.
     *
     * @param ex The exception containing details about the missing resource path.
     * @return A redirect directive to the root URL.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFoundError(NoResourceFoundException ex) {
        // ex.getResourcePath() will print the exact path that was missing
        logger.warn("Resource not found: {}. Redirecting user to home.", ex.getResourcePath());

        return REDIRECT_HOME;
    }
}