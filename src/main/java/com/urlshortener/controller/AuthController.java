package com.urlshortener.controller;

import com.urlshortener.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller handling user authentication requests: entry routing, registration, and login.
 * Redirects already-authenticated users away from auth forms back to the dashboard.
 */
@Controller
public class AuthController {

    private final UserService userService;

    /**
     * Constructs the authentication controller.
     *
     * @param userService the service to manage user registration and loading
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Renders the landing path. If a user is already authenticated, redirects them to dashboard.
     * Otherwise, redirects them to the login screen.
     *
     * @return redirection path
     */
    @GetMapping("/")
    public String index() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    /**
     * Renders the login page template. If a user is already authenticated, redirects to dashboard.
     *
     * @param auth the current user authentication details
     * @return the login view name or redirection path
     */
    @GetMapping("/login")
    public String login(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    /**
     * Renders the registration page template. If a user is already authenticated, redirects to dashboard.
     *
     * @param auth the current user authentication details
     * @return the register view name or redirection path
     */
    @GetMapping("/register")
    public String register(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    /**
     * Processes registration form requests. Creates a new user if parameters are valid.
     * Redirects to login page on success, or stays on register page with error feedback on failure.
     *
     * @param username the submitted registration username
     * @param password the submitted registration password
     * @param model    the MVC Model context to pass error data back to view
     * @return redirection or view path
     */
    @PostMapping("/register")
    public String processRegister(@RequestParam("username") String username,
                                  @RequestParam("password") String password,
                                  Model model) {
        try {
            userService.registerUser(username, password);
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            return "register";
        }
    }
}
