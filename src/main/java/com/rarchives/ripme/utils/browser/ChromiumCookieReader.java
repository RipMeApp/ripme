package com.rarchives.ripme.utils.browser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.json.JSONObject;

/**
 * Reads cookies from Chrome, Edge, Brave and Chromium.
 * <p>
 * Values are encrypted. Decryption is supported for the schemes that can be undone from outside the
 * browser: AES-GCM with a DPAPI-protected key (Windows, {@code v10}) and AES-CBC with a keychain
 * or fixed key (macOS / Linux, {@code v10}). Cookies protected by app-bound encryption ({@code v20})
 * are counted as undecryptable so the caller can suggest an alternative.
 */
final class ChromiumCookieReader implements CookieReader {

    private static final long WINDOWS_TO_UNIX_EPOCH_SECONDS = 11_644_473_600L;
    private static final String DPAPI_PREFIX = "DPAPI";

    private final Browser browser;

    ChromiumCookieReader(Browser browser) {
        if (!browser.isChromiumBased()) {
            throw new IllegalArgumentException(browser + " is not Chromium based");
        }
        this.browser = browser;
    }

    @Override
    public Browser browser() {
        return browser;
    }

    @Override
    public boolean isInstalled() {
        Path dir = userDataDir();
        return dir != null && Files.isDirectory(dir);
    }

    @Override
    public ReadResult read(String host) throws IOException {
        List<String> notes = new ArrayList<>();
        Path userData = userDataDir();
        if (userData == null || !Files.isDirectory(userData)) {
            return new ReadResult(List.of(), 0, notes);
        }

        Map<String, byte[]> keys = null;
        boolean keysLoaded = false;
        List<BrowserCookie> cookies = new ArrayList<>();
        int undecryptable = 0;

        for (Path cookieDb : cookieDatabases(userData)) {
            try {
                if (!keysLoaded) {
                    keysLoaded = true;
                    try {
                        keys = loadKeys(userData);
                    } catch (IOException | GeneralSecurityException | RuntimeException e) {
                        keys = new HashMap<>();
                        notes.add(browser + ": could not obtain the cookie decryption key (" + e.getMessage() + ")");
                    }
                }
                int[] failures = new int[1];
                cookies.addAll(readDatabase(cookieDb, host, keys, isWindows(), failures));
                undecryptable += failures[0];
                if (!cookies.isEmpty()) {
                    break;
                }
            } catch (IOException | SQLException e) {
                notes.add(browser + " (" + cookieDb.getParent().getFileName() + "): " + e.getMessage()
                        + " - close the browser and try again if the cookie file is locked");
            }
        }
        return new ReadResult(cookies, undecryptable, notes);
    }

    /** Reads and decrypts the cookies for {@code host} out of one Chromium cookie database. */
    static List<BrowserCookie> readDatabase(Path cookieDb, String host, Map<String, byte[]> keys, boolean gcm,
            int[] undecryptableOut) throws IOException, SQLException {
        List<String> hostKeys = CookieDomains.candidateHostKeys(host);
        if (hostKeys.isEmpty()) {
            return List.of();
        }
        long nowSeconds = System.currentTimeMillis() / 1000;
        List<BrowserCookie> result = new ArrayList<>();
        try (TempDatabase copy = TempDatabase.copyOf(cookieDb);
                Connection connection = copy.open();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT host_key, name, value, encrypted_value, expires_utc FROM cookies WHERE host_key IN ("
                                + TempDatabase.placeholders(hostKeys.size()) + ")")) {
            for (int i = 0; i < hostKeys.size(); i++) {
                statement.setString(i + 1, hostKeys.get(i));
            }
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    long expiresUtc = rows.getLong("expires_utc");
                    if (expiresUtc > 0 && expiresUtc / 1_000_000 - WINDOWS_TO_UNIX_EPOCH_SECONDS < nowSeconds) {
                        continue;
                    }
                    String hostKey = rows.getString("host_key");
                    String name = rows.getString("name");
                    String value = rows.getString("value");
                    if (value == null || value.isEmpty()) {
                        byte[] encrypted = rows.getBytes("encrypted_value");
                        if (encrypted == null || encrypted.length == 0) {
                            continue;
                        }
                        try {
                            value = ChromiumCookieDecryptor.decrypt(encrypted, keys, hostKey, gcm);
                        } catch (GeneralSecurityException | ChromiumCookieDecryptor.UnsupportedEncryptionException e) {
                            undecryptableOut[0]++;
                            continue;
                        }
                    }
                    if (!value.isEmpty()) {
                        result.add(new BrowserCookie(hostKey, name, value));
                    }
                }
            }
        }
        return result;
    }

    // ---- locating files ----------------------------------------------------------------

    private static boolean isWindows() {
        return osName().contains("win");
    }

    private static boolean isMac() {
        return osName().contains("mac");
    }

    private static String osName() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
    }

    Path userDataDir() {
        String home = System.getProperty("user.home", "");
        if (isWindows()) {
            String local = System.getenv("LOCALAPPDATA");
            if (local == null) {
                return null;
            }
            return switch (browser) {
                case CHROME -> Paths.get(local, "Google", "Chrome", "User Data");
                case EDGE -> Paths.get(local, "Microsoft", "Edge", "User Data");
                case BRAVE -> Paths.get(local, "BraveSoftware", "Brave-Browser", "User Data");
                case CHROMIUM -> Paths.get(local, "Chromium", "User Data");
                default -> null;
            };
        }
        if (isMac()) {
            Path support = Paths.get(home, "Library", "Application Support");
            return switch (browser) {
                case CHROME -> support.resolve(Paths.get("Google", "Chrome"));
                case EDGE -> support.resolve("Microsoft Edge");
                case BRAVE -> support.resolve(Paths.get("BraveSoftware", "Brave-Browser"));
                case CHROMIUM -> support.resolve("Chromium");
                default -> null;
            };
        }
        String xdg = System.getenv("XDG_CONFIG_HOME");
        Path config = (xdg != null && !xdg.isBlank()) ? Paths.get(xdg) : Paths.get(home, ".config");
        return switch (browser) {
            case CHROME -> config.resolve("google-chrome");
            case EDGE -> config.resolve("microsoft-edge");
            case BRAVE -> config.resolve(Paths.get("BraveSoftware", "Brave-Browser"));
            case CHROMIUM -> config.resolve("chromium");
            default -> null;
        };
    }

    /** Cookie database of every profile, "Default" first. */
    private static List<Path> cookieDatabases(Path userData) throws IOException {
        List<Path> profiles = new ArrayList<>();
        try (Stream<Path> children = Files.list(userData)) {
            children.filter(Files::isDirectory).forEach(dir -> {
                String name = dir.getFileName().toString();
                if (name.equals("Default") || name.startsWith("Profile ")) {
                    profiles.add(dir);
                }
            });
        }
        profiles.sort((a, b) -> {
            boolean aDefault = a.getFileName().toString().equals("Default");
            boolean bDefault = b.getFileName().toString().equals("Default");
            if (aDefault != bDefault) {
                return aDefault ? -1 : 1;
            }
            return a.getFileName().toString().compareTo(b.getFileName().toString());
        });
        List<Path> databases = new ArrayList<>();
        for (Path profile : profiles) {
            Path network = profile.resolve("Network").resolve("Cookies");
            Path legacy = profile.resolve("Cookies");
            if (Files.isRegularFile(network)) {
                databases.add(network);
            } else if (Files.isRegularFile(legacy)) {
                databases.add(legacy);
            }
        }
        return databases;
    }

    // ---- decryption keys ---------------------------------------------------------------

    private Map<String, byte[]> loadKeys(Path userData) throws IOException, GeneralSecurityException {
        Map<String, byte[]> keys = new HashMap<>();
        if (isWindows()) {
            keys.put("v10", windowsKey(userData));
        } else if (isMac()) {
            String[] service = macKeychainEntry();
            String password = run(List.of("security", "find-generic-password", "-w", "-s", service[0], "-a", service[1]),
                    Map.of()).trim();
            keys.put("v10", ChromiumCookieDecryptor.derivePosixKey(password, 1003));
        } else {
            // v10 on Linux uses a fixed password; v11 (keyring) is not supported.
            keys.put("v10", ChromiumCookieDecryptor.derivePosixKey("peanuts", 1));
        }
        return keys;
    }

    private String[] macKeychainEntry() {
        return switch (browser) {
            case EDGE -> new String[] { "Microsoft Edge Safe Storage", "Microsoft Edge" };
            case BRAVE -> new String[] { "Brave Safe Storage", "Brave" };
            case CHROMIUM -> new String[] { "Chromium Safe Storage", "Chromium" };
            default -> new String[] { "Chrome Safe Storage", "Chrome" };
        };
    }

    private static byte[] windowsKey(Path userData) throws IOException {
        String localState = Files.readString(userData.resolve("Local State"), StandardCharsets.UTF_8);
        String encodedKey = new JSONObject(localState).getJSONObject("os_crypt").getString("encrypted_key");
        byte[] blob = Base64.getDecoder().decode(encodedKey);
        if (blob.length <= DPAPI_PREFIX.length()
                || !new String(blob, 0, DPAPI_PREFIX.length(), StandardCharsets.US_ASCII).equals(DPAPI_PREFIX)) {
            throw new IOException("unexpected key format in Local State");
        }
        byte[] protectedKey = java.util.Arrays.copyOfRange(blob, DPAPI_PREFIX.length(), blob.length);
        String script = "Add-Type -AssemblyName System.Security; "
                + "[Convert]::ToBase64String([System.Security.Cryptography.ProtectedData]::Unprotect("
                + "[Convert]::FromBase64String($env:RIPME_DPAPI_BLOB), $null, "
                + "[System.Security.Cryptography.DataProtectionScope]::CurrentUser))";
        String output = run(List.of("powershell", "-NoProfile", "-NonInteractive", "-Command", script),
                Map.of("RIPME_DPAPI_BLOB", Base64.getEncoder().encodeToString(protectedKey))).trim();
        return Base64.getDecoder().decode(output);
    }

    private static String run(List<String> command, Map<String, String> environment) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.environment().putAll(environment);
        builder.redirectErrorStream(false);
        Process process = builder.start();
        try {
            if (!process.waitFor(20, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IOException("timed out running " + command.get(0));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            throw new IOException("interrupted running " + command.get(0), e);
        }
        if (process.exitValue() != 0) {
            throw new IOException(command.get(0) + " failed with exit code " + process.exitValue());
        }
        return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
