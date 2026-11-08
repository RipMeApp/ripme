package com.rarchives.ripme.utils.browser;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads cookies from Firefox's {@code cookies.sqlite}. Firefox stores cookie values unencrypted.
 */
final class FirefoxCookieReader implements CookieReader {

    @Override
    public Browser browser() {
        return Browser.FIREFOX;
    }

    @Override
    public boolean isInstalled() {
        return !FirefoxProfiles.profilesWithCookies().isEmpty();
    }

    @Override
    public ReadResult read(String host) throws IOException {
        List<String> notes = new ArrayList<>();
        for (Path profile : FirefoxProfiles.profilesWithCookies()) {
            try {
                List<BrowserCookie> cookies = readDatabase(profile.resolve("cookies.sqlite"), host);
                if (!cookies.isEmpty()) {
                    return new ReadResult(cookies, 0, notes);
                }
            } catch (IOException | SQLException e) {
                notes.add("Firefox profile " + profile.getFileName() + ": " + e.getMessage());
            }
        }
        return new ReadResult(List.of(), 0, notes);
    }

    /** Reads the cookies for {@code host} out of one cookies.sqlite file. */
    static List<BrowserCookie> readDatabase(Path cookieDb, String host) throws IOException, SQLException {
        List<String> hostKeys = CookieDomains.candidateHostKeys(host);
        if (hostKeys.isEmpty()) {
            return List.of();
        }
        long nowSeconds = System.currentTimeMillis() / 1000;
        List<BrowserCookie> result = new ArrayList<>();
        try (TempDatabase copy = TempDatabase.copyOf(cookieDb);
                Connection connection = copy.open();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT host, name, value, expiry FROM moz_cookies WHERE host IN ("
                                + TempDatabase.placeholders(hostKeys.size()) + ")")) {
            for (int i = 0; i < hostKeys.size(); i++) {
                statement.setString(i + 1, hostKeys.get(i));
            }
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    long expiry = rows.getLong("expiry");
                    if (expiry > 100_000_000_000L) { // some Firefox versions store milliseconds
                        expiry /= 1000;
                    }
                    if (expiry > 0 && expiry < nowSeconds) {
                        continue;
                    }
                    String value = rows.getString("value");
                    if (value == null || value.isEmpty()) {
                        continue;
                    }
                    result.add(new BrowserCookie(rows.getString("host"), rows.getString("name"), value));
                }
            }
        }
        return result;
    }
}
