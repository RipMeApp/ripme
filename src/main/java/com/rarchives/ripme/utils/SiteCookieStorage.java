package com.rarchives.ripme.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Stores per-domain cookie strings in the config directory, outside rip.properties.
 */
public final class SiteCookieStorage {

    private SiteCookieStorage() {
    }

    public static String normalizeDomain(String domain) {
        String normalized = domain.trim().toLowerCase();
        if (normalized.startsWith("www.")) {
            normalized = normalized.substring(4);
        }
        return normalized;
    }

    public static String normalizeCookieString(String raw) {
        String normalized = raw.trim();
        if (normalized.regionMatches(true, 0, "cookie:", 0, 7)) {
            normalized = normalized.substring(7).trim();
        }
        return normalized;
    }

    public static Path cookieFilePath(String domain) {
        return Paths.get(Utils.getConfigDir(), "cookies." + normalizeDomain(domain));
    }

    public static String load(String domain) throws IOException {
        String normalizedDomain = normalizeDomain(domain);
        String fromConfig = Utils.getConfigString("cookies." + normalizedDomain, "");
        if (!fromConfig.isEmpty()) {
            return fromConfig;
        }
        Path cookieFile = cookieFilePath(normalizedDomain);
        if (!Files.exists(cookieFile)) {
            return "";
        }
        return Files.readString(cookieFile).trim();
    }

    public static void save(String domain, String raw) throws IOException {
        String cookieString = normalizeCookieString(raw);
        Map<String, String> cookies = RipUtils.getCookiesFromString(cookieString);
        if (cookies.isEmpty()) {
            throw new IllegalArgumentException("No valid name=value cookie pairs found");
        }
        Path configDir = Paths.get(Utils.getConfigDir());
        Files.createDirectories(configDir);
        Files.writeString(cookieFilePath(domain), cookieString);
    }

    public static void clear(String domain) throws IOException {
        Files.deleteIfExists(cookieFilePath(domain));
    }
}
