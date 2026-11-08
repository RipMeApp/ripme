package com.rarchives.ripme.utils;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.utils.browser.BrowserCookieImporter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper around the Jsoup connection methods.
 * <p>
 * Benefit is retry logic.
 */
public class Http {

    private static final int TIMEOUT = Utils.getConfigInteger("page.timeout", 5 * 1000);
    private static final Logger logger = LogManager.getLogger(Http.class);

    private int retries;
    private int retrySleep = 0;
    private final String url;
    private Connection connection;

    // Constructors
    public Http(String url) {
        this.url = url;
        defaultSettings();
    }

    private Http(URL url) {
        this.url = url.toExternalForm();
        defaultSettings();
    }

    public static Http url(String url) {
        return new Http(url);
    }

    public static Http url(URL url) {
        return new Http(url);
    }

    private void defaultSettings() {
        this.retries = Utils.getConfigInteger("download.retries", 3);
        this.retrySleep = Utils.getConfigInteger("download.retry.sleep", 5000);
        connection = Jsoup.connect(this.url);
        connection.userAgent(AbstractRipper.USER_AGENT);
        connection.method(Method.GET);
        connection.timeout(TIMEOUT);
        connection.maxBodySize(0);

        // Extract cookies from config entry:
        // Example config entry:
        // cookies.reddit.com = reddit_session=<value>; other_cookie=<value>
        connection.cookies(cookiesForURL(this.url));
    }

    /**
     * Cookies stored for the host of the given URL (or one of its parent domains), as saved
     * in Configuration → Site cookies.
     */
    public static Map<String, String> cookiesForURL(String u) {
        Map<String, String> cookiesParsed = new HashMap<>();

        String cookieDomain = "";
        try {
            URL parsed = new URI(u).toURL();
            String cookieStr = "";

            String[] parts = parsed.getHost().split("\\.");

            // if url is www.reddit.com, we should also use cookies from reddit.com;
            // this rule is applied for all subdomains (for all rippers); e.g. also
            // old.reddit.com, new.reddit.com
            while (parts.length > 1) {
                String domain = String.join(".", parts);
                // Try to get cookies for this host from config
                logger.debug("Trying to load cookies from config for " + domain);
                try {
                    cookieStr = SiteCookieStorage.load(domain);
                } catch (IOException e) {
                    logger.warn("Failed to load cookies for {}", domain, e);
                    cookieStr = "";
                }
                if (!cookieStr.equals("")) {
                    cookieDomain = domain;
                    // we found something, start parsing
                    break;
                }
                parts = (String[]) ArrayUtils.remove(parts, 0);
            }

            if (!cookieStr.equals("")) {
                cookiesParsed = RipUtils.getCookiesFromString(cookieStr.trim());
            }
        } catch (MalformedURLException | URISyntaxException e) {
            logger.warn("Parsing url " + u + " while getting cookies", e);
        }

        if (cookiesParsed.size() > 0) {
            logger.info("Cookies for " + cookieDomain + " have been added to this request");
        }

        return cookiesParsed;
    }

    // Setters
    public Http timeout(int timeout) {
        connection.timeout(timeout);
        return this;
    }

    public Http ignoreContentType() {
        connection.ignoreContentType(true);
        return this;
    }

    public Http referrer(String ref) {
        connection.referrer(ref);
        return this;
    }

    public Http referrer(URL ref) {
        return referrer(ref.toExternalForm());
    }

    public Http userAgent(String ua) {
        connection.userAgent(ua);
        return this;
    }

    public Http retries(int tries) {
        this.retries = tries;
        return this;
    }

    public Http header(String name, String value) {
        connection.header(name, value);
        return this;
    }

    public Http cookies(Map<String, String> cookies) {
        connection.cookies(cookies);
        return this;
    }

    public Http data(Map<String, String> data) {
        connection.data(data);
        return this;
    }

    public Http data(String name, String value) {
        Map<String, String> data = new HashMap<>();
        data.put(name, value);
        return data(data);
    }

    public Http method(Method method) {
        connection.method(method);
        return this;
    }

    // Getters
    public Connection connection() {
        return connection;
    }

    public Document get() throws IOException {
        connection.method(Method.GET);
        return response().parse();
    }

    public Document post() throws IOException {
        connection.method(Method.POST);
        return response().parse();
    }

    public JSONObject getJSON() throws IOException {
        ignoreContentType();
        String jsonString = response().body();
        return new JSONObject(jsonString);
    }

    public JSONArray getJSONArray() throws IOException {
        ignoreContentType();
        String jsonArray = response().body();
        return new JSONArray(jsonArray);
    }

    public Response response() throws IOException {
        Response response;
        IOException lastException = null;
        int retries = this.retries;
        boolean browserImportAttempted = false;
        while (--retries >= 0) {
            try {
                response = connection.execute();
                return response;
            } catch (IOException e) {
                // Warn users about possibly fixable permission error
                if (e instanceof org.jsoup.HttpStatusException) {
                    HttpStatusException ex = (HttpStatusException) e;

                    // These status codes might indicate missing cookies
                    //     401 Unauthorized
                    //     403 Forbidden

                    int status = ex.getStatusCode();
                    if (status == 401 || status == 403) {
                        // Optional: borrow the session cookies of the user's own browser and retry once.
                        if (!browserImportAttempted && Utils.getConfigBoolean(BrowserCookieImporter.CONFIG_AUTO_IMPORT, false)) {
                            browserImportAttempted = true;
                            if (importCookiesFromBrowser()) {
                                retries++; // the import is not a failed attempt
                                continue;
                            }
                        }
                        throw new IOException("Failed to load " + url + ": Status Code " + status + ". You might be able to circumvent this error by setting cookies for this domain (Configuration → Site cookies... → Import from browser)", e);
                    }
                    if (status == 404) {
                        throw new IOException("File not found " + url + ": Status Code " + status + ". ", e);
                    }
                }

                if (retrySleep > 0 && retries >= 0) {
                    logger.warn("Error while loading " + url + " waiting "+ retrySleep + " ms before retrying.", e);
                    Utils.sleep(retrySleep);
                } else {
                    logger.warn("Error while loading " + url, e);
                }
                lastException = e;
            }
        }
        throw new IOException("Failed to load " + url + " after " + this.retries + " attempts", lastException);
    }

    /**
     * Imports this request's site cookies from the browser the user is running, stores them
     * for later requests and adds them to the current connection.
     *
     * @return true if cookies were found and applied
     */
    private boolean importCookiesFromBrowser() {
        BrowserCookieImporter.ImportResult result = BrowserCookieImporter.importFor(url);
        if (!result.found()) {
            logger.info("No browser cookies found for {}", result.host());
            return false;
        }
        try {
            SiteCookieStorage.save(result.host(), result.cookieHeader());
        } catch (IOException | IllegalArgumentException ex) {
            logger.warn("Could not store imported cookies for {}", result.host(), ex);
        }
        connection.cookies(result.cookies());
        logger.info("Retrying {} with cookies imported from {}", url, result.browser());
        return true;
    }

    public static void SSLVerifyOff() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            logger.error("ignoreSSLVerification() failed.");
            logger.error(e.getMessage());
        }
    }

    public static void undoSSLVerifyOff() {
        try {
            // Reset to the default SSL socket factory and hostname verifier
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, null, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
        } catch (Exception e) {
            logger.error("undoSSLVerificationIgnore() failed.");
            logger.error(e.getMessage());
        }
    }
}
