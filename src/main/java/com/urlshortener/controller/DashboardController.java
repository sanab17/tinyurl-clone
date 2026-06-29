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

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final ShortUrlService shortUrlService;
    private final UserService userService;

    public DashboardController(ShortUrlService shortUrlService, UserService userService) {
        this.shortUrlService = shortUrlService;
        this.userService = userService;
    }

    private User getCurrentUser(Principal principal) {
        return userService.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

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
            redirectAttributes.addFlashAttribute("successMessage", "Short link created successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("originalUrl", originalUrl);
            redirectAttributes.addFlashAttribute("customAlias", customAlias);
            redirectAttributes.addFlashAttribute("title", title);
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/delete/{id}")
    public String deleteShortUrl(@PathVariable("id") Long id, Principal principal, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        try {
            shortUrlService.deleteShortUrl(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Short link deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/analytics/{code}")
    public String viewAnalytics(@PathVariable("code") String code, Model model, Principal principal, HttpServletRequest request) {
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
                        Collectors.counting()
                ));

        // 2. Clicks by Browser
        Map<String, Long> clicksByBrowser = clickLogs.stream()
                .collect(Collectors.groupingBy(
                        ClickAnalytic::getBrowser,
                        Collectors.counting()
                ));

        // 3. Clicks by Operating System
        Map<String, Long> clicksByOs = clickLogs.stream()
                .collect(Collectors.groupingBy(
                        ClickAnalytic::getOperatingSystem,
                        Collectors.counting()
                ));

        // 4. Clicks by Referrer
        Map<String, Long> clicksByReferrer = clickLogs.stream()
                .collect(Collectors.groupingBy(
                        ClickAnalytic::getReferrer,
                        Collectors.counting()
                ));

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
