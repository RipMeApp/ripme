package com.rarchives.ripme.utils.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BrowserCookieUnitTest {

    // ---- domain matching ------------------------------------------------------------------

    @Test
    void candidateHostKeysIncludeParentDomainsButNotTheTld() {
        assertEquals(List.of("www.reddit.com", ".www.reddit.com", "reddit.com", ".reddit.com"),
                CookieDomains.candidateHostKeys("www.reddit.com"));
        assertFalse(CookieDomains.candidateHostKeys("old.reddit.com").contains("com"));
        assertFalse(CookieDomains.candidateHostKeys("old.reddit.com").contains(".com"));
    }

    @Test
    void candidateHostKeysForIpAddressIsOnlyTheAddress() {
        assertEquals(List.of("192.168.0.1"), CookieDomains.candidateHostKeys("192.168.0.1"));
    }

    @Test
    void hostOfAcceptsUrlsAndBareHosts() {
        assertEquals("www.reddit.com", CookieDomains.hostOf("https://www.reddit.com/r/pics/"));
        assertEquals("reddit.com", CookieDomains.hostOf("Reddit.com"));
        assertEquals("", CookieDomains.hostOf("  "));
    }

    @Test
    void processNamesAreRecognised() {
        assertEquals("msedge", BrowserDetector.normalize("/opt/edge/MSEdge.exe"));
        assertEquals("google-chrome", BrowserDetector.normalize("/Applications/Google Chrome"));
        assertTrue(Browser.CHROME.matchesProcess("google-chrome"));
        assertTrue(Browser.FIREFOX.matchesProcess("firefox"));
        assertFalse(Browser.FIREFOX.matchesProcess("chrome"));
    }

    // ---- Chromium decryption --------------------------------------------------------------

    private static byte[] sha256(String text) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] concat(byte[]... parts) {
        int length = 0;
        for (byte[] part : parts) {
            length += part.length;
        }
        byte[] result = new byte[length];
        int offset = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, result, offset, part.length);
            offset += part.length;
        }
        return result;
    }

    static byte[] encryptGcm(byte[] key, String plain, String hostKey, boolean withHostHash) throws Exception {
        byte[] nonce = new byte[12];
        new SecureRandom().nextBytes(nonce);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
        byte[] body = plain.getBytes(StandardCharsets.UTF_8);
        if (withHostHash) {
            body = concat(sha256(hostKey), body);
        }
        return concat("v10".getBytes(StandardCharsets.US_ASCII), nonce, cipher.doFinal(body));
    }

    static byte[] encryptCbc(byte[] key, String plain) throws Exception {
        byte[] iv = new byte[16];
        Arrays.fill(iv, (byte) ' ');
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        return concat("v10".getBytes(StandardCharsets.US_ASCII),
                cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void decryptsWindowsGcmValuesWithAndWithoutHostHash() throws Exception {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        Map<String, byte[]> keys = Map.of("v10", key);

        byte[] oldStyle = encryptGcm(key, "session-value", ".reddit.com", false);
        byte[] newStyle = encryptGcm(key, "session-value", ".reddit.com", true);
        assertEquals("session-value", ChromiumCookieDecryptor.decrypt(oldStyle, keys, ".reddit.com", true));
        assertEquals("session-value", ChromiumCookieDecryptor.decrypt(newStyle, keys, ".reddit.com", true));
    }

    @Test
    void decryptsPosixCbcValues() throws Exception {
        byte[] key = ChromiumCookieDecryptor.derivePosixKey("peanuts", 1);
        assertEquals(16, key.length);
        byte[] encrypted = encryptCbc(key, "token=abc");
        assertEquals("token=abc",
                ChromiumCookieDecryptor.decrypt(encrypted, Map.of("v10", key), ".example.com", false));
    }

    @Test
    void appBoundEncryptionIsReportedAsUnsupported() {
        byte[] v20 = concat("v20".getBytes(StandardCharsets.US_ASCII), new byte[40]);
        assertThrows(ChromiumCookieDecryptor.UnsupportedEncryptionException.class,
                () -> ChromiumCookieDecryptor.decrypt(v20, Map.of("v10", new byte[32]), ".x.com", true));
    }

    @Test
    void wrongKeyFailsInsteadOfReturningGarbage() throws Exception {
        byte[] key = new byte[32];
        byte[] wrong = new byte[32];
        wrong[0] = 1;
        byte[] encrypted = encryptGcm(key, "secret", ".x.com", false);
        assertThrows(java.security.GeneralSecurityException.class,
                () -> ChromiumCookieDecryptor.decrypt(encrypted, Map.of("v10", wrong), ".x.com", true));
    }

    // ---- Firefox profiles.ini ---------------------------------------------------------------

    @Test
    void profilesIniListsDefaultProfilesFirst(@TempDir Path root) throws Exception {
        Path ini = root.resolve("profiles.ini");
        Files.writeString(ini, String.join("\n",
                "[Profile1]", "Name=work", "IsRelative=1", "Path=Profiles/work", "",
                "[Profile0]", "Name=default-release", "IsRelative=1", "Path=Profiles/abc.default-release",
                "Default=1", "",
                "[Install4F96D1932A9F858E]", "Default=Profiles/abc.default-release", "Locked=1", "",
                "[Profile2]", "Name=abs", "IsRelative=0", "Path=/tmp/abs-profile", ""));
        List<Path> profiles = FirefoxProfiles.parseProfilesIni(ini);
        assertEquals(root.resolve("Profiles/abc.default-release"), profiles.get(0));
        assertEquals(3, profiles.size());
        assertTrue(profiles.contains(root.resolve("Profiles/work")));
        assertTrue(profiles.contains(java.nio.file.Paths.get("/tmp/abs-profile")));
    }

    // ---- SQLite readers (need sqlite-jdbc on the classpath) -----------------------------------

    @Test
    void readsFirefoxCookiesForTheRequestedHostOnly(@TempDir Path dir) throws Exception {
        Path db = dir.resolve("cookies.sqlite");
        long future = System.currentTimeMillis() / 1000 + 3600;
        long past = System.currentTimeMillis() / 1000 - 3600;
        try (var c = java.sql.DriverManager.getConnection("jdbc:sqlite:" + db); var s = c.createStatement()) {
            s.execute("CREATE TABLE moz_cookies (host TEXT, name TEXT, value TEXT, expiry INTEGER)");
            s.execute("INSERT INTO moz_cookies VALUES ('.reddit.com','reddit_session','abc'," + future + ")");
            s.execute("INSERT INTO moz_cookies VALUES ('www.reddit.com','loid','def'," + future + ")");
            s.execute("INSERT INTO moz_cookies VALUES ('.reddit.com','old','gone'," + past + ")");
            s.execute("INSERT INTO moz_cookies VALUES ('.example.org','other','nope'," + future + ")");
        }
        var cookies = FirefoxCookieReader.readDatabase(db, "www.reddit.com");
        assertEquals(2, cookies.size());
        assertTrue(cookies.stream().anyMatch(c -> c.name().equals("reddit_session") && c.value().equals("abc")));
        assertTrue(cookies.stream().anyMatch(c -> c.name().equals("loid")));
        assertFalse(cookies.stream().anyMatch(c -> c.name().equals("other") || c.name().equals("old")));
    }

    @Test
    void readsAndDecryptsChromiumCookies(@TempDir Path dir) throws Exception {
        Path db = dir.resolve("Cookies");
        byte[] key = ChromiumCookieDecryptor.derivePosixKey("peanuts", 1);
        byte[] encrypted = encryptCbc(key, "secret-token");
        byte[] unsupported = concat("v20".getBytes(StandardCharsets.US_ASCII), new byte[30]);
        try (var c = java.sql.DriverManager.getConnection("jdbc:sqlite:" + db); var s = c.createStatement()) {
            s.execute("CREATE TABLE cookies (host_key TEXT, name TEXT, value TEXT, encrypted_value BLOB, "
                    + "expires_utc INTEGER)");
            try (var p = c.prepareStatement("INSERT INTO cookies VALUES (?,?,?,?,0)")) {
                p.setString(1, ".reddit.com");
                p.setString(2, "token_v2");
                p.setString(3, "");
                p.setBytes(4, encrypted);
                p.executeUpdate();
                p.setString(2, "plain");
                p.setString(3, "visible");
                p.setBytes(4, new byte[0]);
                p.executeUpdate();
                p.setString(2, "appbound");
                p.setString(3, "");
                p.setBytes(4, unsupported);
                p.executeUpdate();
            }
        }
        int[] failures = new int[1];
        var cookies = ChromiumCookieReader.readDatabase(db, "www.reddit.com", Map.of("v10", key), false, failures);
        assertEquals(2, cookies.size());
        assertTrue(cookies.stream().anyMatch(c -> c.name().equals("token_v2") && c.value().equals("secret-token")));
        assertTrue(cookies.stream().anyMatch(c -> c.name().equals("plain") && c.value().equals("visible")));
        assertEquals(1, failures[0]);
    }
}
