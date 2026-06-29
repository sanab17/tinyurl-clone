package com.urlshortener.util;

public class UserAgentParser {

    public static String parseBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }
        String ua = userAgent.toLowerCase();
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
