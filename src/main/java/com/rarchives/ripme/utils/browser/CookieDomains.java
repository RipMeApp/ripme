package com.rarchives.ripme.utils.browser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Domain matching rules shared by the browser cookie readers. They mirror how
 * {@code Http} looks up stored cookies: a request to www.reddit.com uses cookies of
 * www.reddit.com and reddit.com, never of a bare top-level domain.
 */
final class CookieDomains {

    private static final Pattern IPV4 = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}");

    private CookieDomains() {
    }

    /** Extracts the host from a URL or a bare host name; returns "" when there is none. */
    static String hostOf(String urlOrHost) {
        if (urlOrHost == null) {
            return "";
        }
        String text = urlOrHost.trim();
        if (text.isEmpty()) {
            return "";
        }
        try {
            URI uri = new URI(text.contains("://") ? text : "https://" + text);
            String host = uri.getHost();
            return host == null ? "" : host.toLowerCase(Locale.ROOT);
        } catch (URISyntaxException e) {
            return "";
        }
    }

    /**
     * All values of a cookie's domain column that apply to the given host, both with and
     * without the leading dot browsers use for domain cookies.
     */
    static List<String> candidateHostKeys(String host) {
        String h = host.toLowerCase(Locale.ROOT);
        List<String> keys = new ArrayList<>();
        if (h.isEmpty()) {
            return keys;
        }
        if (h.contains(":") || IPV4.matcher(h).matches()) {
            keys.add(h);
            return keys;
        }
        String[] parts = h.split("\\.");
        for (int i = 0; parts.length - i >= 2; i++) {
            String domain = String.join(".", Arrays.copyOfRange(parts, i, parts.length));
            keys.add(domain);
            keys.add("." + domain);
        }
        if (keys.isEmpty()) {
            keys.add(h);
        }
        return keys;
    }
}
