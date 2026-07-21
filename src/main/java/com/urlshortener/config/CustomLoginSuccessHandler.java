package com.urlshortener.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;

/**
 * Custom authentication success handler that redirects the user to their dashboard after a successful login.
 * It also populates a Spring MVC flash attribute to display a welcome notification.
 */
public class CustomLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    /**
     * Called when a user has been successfully authenticated.
     * Sets a welcome message in the flash attributes, clears any cached authentication errors from the session,
     * and redirects the user to the dashboard.
     *
     * @param request        the request that initiated the authentication
     * @param response       the response to write redirection headers to
     * @param authentication the authenticated token
     * @throws IOException      if redirect fails or an input/output exception occurs
     * @throws ServletException if any servlet exception occurs
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        FlashMap flashMap = new FlashMap();
        flashMap.put("successMessage", "Welcome back! You have successfully logged in.");
        
        // Since Spring Security runs before DispatcherServlet, we instantiate the manager directly
        FlashMapManager flashMapManager = new SessionFlashMapManager();
        flashMapManager.saveOutputFlashMap(flashMap, request, response);

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, "/dashboard");
    }
}
