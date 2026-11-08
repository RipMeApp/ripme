package com.rarchives.ripme.utils.browser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rarchives.ripme.utils.browser.CookieReader.BrowserCookie;
import com.rarchives.ripme.utils.browser.CookieReader.ReadResult;

/**
 * Imports the cookies of one website from the browser the user is currently using.
 * <p>
 * Only the cookies that belong to the requested host (and its parent domains) are read. Cookies of
 * every other site are never looked at beyond the database query's host filter, and cookie values
 * are never written to the log.
 */
public final class BrowserCookieImporter {

    /** Config key: when true, a 401/403 response triggers an automatic import. Off by default. */
    public static final String CONFIG_AUTO_IMPORT = "cookies.browser.auto";

    private static final Logger logger = LogManager.getLogger(BrowserCookieImporter.class);

    private BrowserCookieImporter() {
    }

    /**
     * Outcome of an import attempt.
     *
     * @param browser       the browser the cookies came from, or null if none were found
     * @param host          the host the cookies were requested for
     * @param cookies       cookie name to value
     * @param undecryptable matching cookies that exist but could not be decrypted
     * @param notes         reasons for failures, for display to the user
     */
    public record ImportResult(Browser browser, String host, Map<String, String> cookies, int undecryptable,
            List<String> notes) {

        public boolean found() {
            return !cookies.isEmpty();
        }

        /** The cookies as a {@code Cookie:} header value ({@code a=b; c=d}). */
        public String cookieHeader() {
            return cookies.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("; "));
        }
    }

    /**
     * Reads the cookies for a URL or host name from the user's browser. Browsers that are
     * running right now are tried first, then other installed browsers.
     */
    public static ImportResult importFor(String urlOrHost) {
        String host = CookieDomains.hostOf(urlOrHost);
        List<String> notes = new ArrayList<>();
        int undecryptable = 0;
        if (host.isEmpty()) {
            notes.add("No valid host name given");
            return new ImportResult(null, host, Map.of(), 0, notes);
        }

        for (CookieReader reader : readersInPriorityOrder()) {
            try {
                ReadResult result = reader.read(host);
                notes.addAll(result.notes());
                undecryptable += result.undecryptable();
                if (!result.cookies().isEmpty()) {
                    logger.info("Imported {} cookies for {} from {}", result.cookies().size(), host,
                            reader.browser());
                    return new ImportResult(reader.browser(), host, merge(result.cookies()), result.undecryptable(),
                            notes);
                }
            } catch (Exception e) {
                logger.warn("Failed to read {} cookies: {}", reader.browser(), e.getMessage());
                notes.add(reader.browser() + ": " + e.getMessage());
            }
        }
        return new ImportResult(null, host, Map.of(), undecryptable, notes);
    }

    /** Cookies set for more specific domains override those for parent domains. */
    private static Map<String, String> merge(List<BrowserCookie> cookies) {
        List<BrowserCookie> sorted = new ArrayList<>(cookies);
        sorted.sort((a, b) -> Integer.compare(a.hostKey().length(), b.hostKey().length()));
        Map<String, String> merged = new LinkedHashMap<>();
        for (BrowserCookie cookie : sorted) {
            merged.put(cookie.name(), cookie.value());
        }
        return merged;
    }

    private static List<CookieReader> readersInPriorityOrder() {
        Set<Browser> running = BrowserDetector.runningBrowsers();
        Set<Browser> order = new LinkedHashSet<>();
        for (Browser browser : Browser.values()) {
            if (running.contains(browser)) {
                order.add(browser);
            }
        }
        for (Browser browser : Browser.values()) {
            order.add(browser);
        }

        List<CookieReader> readers = new ArrayList<>();
        for (Browser browser : order) {
            CookieReader reader = browser.isChromiumBased() ? new ChromiumCookieReader(browser)
                    : new FirefoxCookieReader();
            if (reader.isInstalled()) {
                readers.add(reader);
            }
        }
        return readers;
    }
}
