package com.urlshortener.controller;

import com.urlshortener.entity.ClickAnalytic;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.entity.User;
import com.urlshortener.service.ShortUrlService;
import com.urlshortener.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Controller responsible for managing the authenticated user's dashboard.
 * Provides views and endpoints to create, delete, and view analytics of shortened links.
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final ShortUrlService shortUrlService;
    private final UserService userService;

    /**
     * Constructs the dashboard controller.
     *
     * @param shortUrlService the service to handle short URL logic
     * @param userService     the service to handle user operations
     */
    public DashboardController(ShortUrlService shortUrlService, UserService userService) {
        this.shortUrlService = shortUrlService;
        this.userService = userService;
    }

    /**
     * Helper method to retrieve the currently logged-in user details.
     *
     * @param principal security principal representing the authenticated user
     * @return the logged-in {@link User} entity
     * @throws UsernameNotFoundException if the user does not exist in the database
     */
    private User getCurrentUser(Principal principal) {
        return userService.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Renders the user's dashboard view.
     * Shows a table of existing short links and key aggregate metrics (total links, total clicks).
     *
     * @param model     the MVC model context
     * @param principal security principal of the logged-in user
     * @param request   the HTTP request used to construct the base short URL domain
     * @return the dashboard view name
     */
    @GetMapping
    public String dashboard(Model model, Principal principal, HttpServletRequest request) {
        User user = getCurrentUser(principal);
        List<ShortUrl> urls = shortUrlService.getUrlsByUser(user);

        long totalUrls = shortUrlService.getUrlCountByUser(user);
        int totalClicks = shortUrlService.getTotalClicksByUser(user);

        // Generate base URL (e.g., http://localhost:8080)
        String baseUrl = request.getScheme() + "://" + request.getHeader("host");

        model.addAttribute("urls", urls);
        model.addAttribute("totalUrls", totalUrls);
        model.addAttribute("totalClicks", totalClicks);
        model.addAttribute("baseUrl", baseUrl);

        return "dashboard";
    }

    /**
     * Handles the creation of a new short URL link.
     * Generates a base64 encoded QR code and registers the URL record in database.
     *
     * @param originalUrl        the destination URL to shorten
     * @param customAlias        optional custom slug/alias requested by the user
     * @param title              optional user-defined description for the link
     * @param principal          security principal of the logged-in user
     * @param request            the HTTP request context to derive base URL domain
     * @param redirectAttributes redirect context to pass temporary success or error flash attributes
     * @return redirection back to dashboard
     */
    @PostMapping("/create")
    public String createShortUrl(@RequestParam("originalUrl") String originalUrl,
            @RequestParam(value = "customAlias", required = false) String customAlias,
            @RequestParam(value = "title", required = false) String title,
            Principal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        String baseUrl = request.getScheme() + "://" + request.getHeader("host");

        try {
            shortUrlService.createShortUrl(originalUrl, customAlias, title, user, baseUrl);
            redirectAttributes.addFlashAttribute("successMessage", "Short link and QR code created successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("originalUrl", originalUrl);
            redirectAttributes.addFlashAttribute("customAlias", customAlias);
            redirectAttributes.addFlashAttribute("title", title);
        }

        return "redirect:/dashboard";
    }

    /**
     * Handles deletion requests for short URLs.
     * Performs owner-validation checks prior to deletion.
     *
     * @param id                 database primary key of the target short URL
     * @param principal          security principal of the logged-in user
     * @param redirectAttributes redirect context for flash status notifications
     * @return redirection back to dashboard
     */
    @GetMapping("/delete/{id}")
    public String deleteShortUrl(@PathVariable("id") Long id, Principal principal,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        try {
            shortUrlService.deleteShortUrl(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Short link has been deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    /**
     * Aggregates and renders detailed analytics reports for a given short URL code.
     * Groups clicks by date, browser type, operating system, and HTTP referrer.
     *
     * @param code      the unique code slug of the short URL
     * @param model     the MVC model context
     * @param principal security principal of the logged-in user
     * @param request   the HTTP request context to derive base URL domain
     * @return the analytics details page view name
     * @throws IllegalArgumentException if the short link code does not exist
     * @throws SecurityException        if the logged-in user is not the owner of the short URL
     */
    @GetMapping("/analytics/{code}")
    public String viewAnalytics(@PathVariable("code") String code, Model model, Principal principal,
            HttpServletRequest request) {
        User user = getCurrentUser(principal);
        ShortUrl shortUrl = shortUrlService.getByShortCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Short link not found"));

        // Security check: verify owner
        if (!shortUrl.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized to view this link's analytics");
        }

        List<ClickAnalytic> clickLogs = shortUrlService.getClicksForUrl(shortUrl);
        List<ClickAnalytic> latestLogs = shortUrlService.getLatestClicksForUrl(shortUrl);

        // Compute aggregate metrics

        // 1. Clicks by Date (TreeMap to keep it sorted chronologically)
        Map<String, Long> clicksByDate = clickLogs.stream()
                .collect(Collectors.groupingBy(
                        click -> click.getClickTime().toLocalDate().toString(),
                        TreeMap::new,
                        Collectors.counting()));

        // 2. Clicks by Browser
        Map<String, Long> clicksByBrowser = clickLogs.stream()
                .collect(Collectors.groupingBy(
                        ClickAnalytic::getBrowser,
                        Collectors.counting()));

        // 3. Clicks by Operating System
        Map<String, Long> clicksByOs = clickLogs.stream()
                .collect(Collectors.groupingBy(
                        ClickAnalytic::getOperatingSystem,
                        Collectors.counting()));

        // 4. Clicks by Referrer
        Map<String, Long> clicksByReferrer = clickLogs.stream()
                .collect(Collectors.groupingBy(
                        ClickAnalytic::getReferrer,
                        Collectors.counting()));

        String baseUrl = request.getScheme() + "://" + request.getHeader("host");

        model.addAttribute("shortUrl", shortUrl);
        model.addAttribute("clicksByDate", clicksByDate);
        model.addAttribute("clicksByBrowser", clicksByBrowser);
        model.addAttribute("clicksByOs", clicksByOs);
        model.addAttribute("clicksByReferrer", clicksByReferrer);
        model.addAttribute("latestLogs", latestLogs.stream().limit(50).collect(Collectors.toList()));
        model.addAttribute("baseUrl", baseUrl);

        return "analytics";
    }
}
