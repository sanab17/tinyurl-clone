package com.urlshortener.util;

/**
 * Utility helper class for extracting operating system and browser names from HTTP User-Agent header strings.
 */
public class UserAgentParser {

    /**
     * Identifies the browser program used by the visitor.
     * Evaluates keywords in the user agent string sequentially (Edge, Chrome, Firefox, Safari, Opera, IE).
     *
     * @param userAgent the raw User-Agent request header value
     * @return the classified browser name, "Unknown" if blank, or "Other" if no match
     */
    public static String parseBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }
        String ua = userAgent.toLowerCase();
        
        // Sequence check to avoid collision (e.g. Chrome/Safari)
        if (ua.contains("edg/")) {
            return "Edge";
        } else if (ua.contains("chrome/") || ua.contains("crios/")) {
            return "Chrome";
        } else if (ua.contains("firefox/") || ua.contains("fxios/")) {
            return "Firefox";
        } else if (ua.contains("safari/") && !ua.contains("chrome") && !ua.contains("chromium")) {
            return "Safari";
        } else if (ua.contains("opr/") || ua.contains("opera/")) {
            return "Opera";
        } else if (ua.contains("msie") || ua.contains("trident/")) {
            return "Internet Explorer";
        }
        return "Other";
    }

    /**
     * Identifies the operating system used by the visitor.
     * Evaluates keywords in the user agent string sequentially (Windows, macOS, iOS, Android, Linux).
     *
     * @param userAgent the raw User-Agent request header value
     * @return the classified operating system name, "Unknown" if blank, or "Other" if no match
     */
    public static String parseOS(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }
        String ua = userAgent.toLowerCase();
        
        if (ua.contains("windows")) {
            return "Windows";
        } else if (ua.contains("macintosh") || ua.contains("mac os x")) {
            return "macOS";
        } else if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod")) {
            return "iOS";
        } else if (ua.contains("android")) {
            return "Android";
        } else if (ua.contains("linux")) {
            return "Linux";
        }
        return "Other";
    }
}
