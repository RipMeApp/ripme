package com.rarchives.ripme.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.helper.StringUtil;
import org.jsoup.Jsoup;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.AbstractRipper;

/**
 * Wrapper around the Jsoup connection methods.
 *
 * Benefit is retry logic.
 */
public class Http {

    private static final int TIMEOUT = Utils.getConfigInteger("page.timeout", 5 * 1000);
    private static final Logger logger = Logger.getLogger(Http.class);

    private int retries;
    private String url;
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
        this.retries = Utils.getConfigInteger("download.retries", 1);
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

    private Map<String, String> cookiesForURL(String u) {
        Map<String, String> cookiesParsed = new HashMap<>();

        String cookieDomain = ""; 
        try {
            URL parsed = new URL(u);
            String cookieStr = "";

            String[] parts = parsed.getHost().split("\\.");

            // if url is www.reddit.com, we should also use cookies from reddit.com;
            // this rule is applied for all subdomains (for all rippers); e.g. also
            // old.reddit.com, new.reddit.com
            while (parts.length > 1) {
                String domain = String.join(".", parts);
                // Try to get cookies for this host from config
                logger.info("Trying to load cookies from config for " + domain);
                cookieStr = Utils.getConfigString("cookies." + domain, "");
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
        } catch (MalformedURLException e) {
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
    public Http referrer(String ref)  {
        connection.referrer(ref);
        return this;
    }
    public Http referrer(URL ref) {
        return referrer(ref.toExternalForm());
    }
    public Http userAgent(String ua)  {
        connection.userAgent(ua);
        return this;
    }
    public Http retries(int tries) {
        this.retries = tries;
        return this;
    }
    public Http header(String name, String value) {
        connection.header(name,  value);
        return this;
    }
    public Http cookies(Map<String,String> cookies) {
        connection.cookies(cookies);
        return this;
    }
    public Http data(Map<String,String> data) {
        connection.data(data);
        return this;
    }
    public Http data(String name, String value) {
        Map<String,String> data = new HashMap<>();
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

    public Response response() throws IOException {
        Response response = null;
        IOException lastException = null;
        int retries = this.retries;
        while (--retries >= 0) {
            try {
                response = connection.execute();
                return response;
            } catch (IOException e) {
                // Warn users about possibly fixable permission error
                if (e instanceof org.jsoup.HttpStatusException) {
                    HttpStatusException ex = (HttpStatusException)e;
                    
                    // These status codes might indicate missing cookies
                    //     401 Unauthorized
                    //     403 Forbidden

                    int status =  ex.getStatusCode();
                    if (status == 401 || status == 403) {
                        throw new IOException("Failed to load " + url + ": Status Code " +  Integer.toString(status) + ". You might be able to circumvent this error by setting cookies for this domain" , e);
                    }
                }

                logger.warn("Error while loading " + url, e);
                lastException = e;
            }
        }
        throw new IOException("Failed to load " + url + " after " + this.retries + " attempts", lastException);
    }
}
