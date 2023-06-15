package com.rarchives.ripme.tst;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import com.rarchives.ripme.utils.Proxy;
import com.rarchives.ripme.utils.Utils;
import com.rarchives.ripme.utils.Http;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class proxyTest  {


    // This test will only run on machines where the user has added a entry for proxy.socks
    @Test
    public void testSocksProxy() throws IOException, URISyntaxException {
        // Unset proxy before testing
        System.setProperty("http.proxyHost", "");
        System.setProperty("https.proxyHost", "");
        System.setProperty("socksProxyHost", "");
        URL url = new URI("https://icanhazip.com").toURL();
        String proxyConfig = Utils.getConfigString("proxy.socks", "");
        if (!proxyConfig.equals("")) {
            String ip1 = Http.url(url).ignoreContentType().get().text();
            Proxy.setSocks(Utils.getConfigString("proxy.socks", ""));
            String ip2 = Http.url(url).ignoreContentType().get().text();
            assertFalse(ip1.equals(ip2));
        } else {
            System.out.println("Skipping testSocksProxy");
            assert(true);
        }
    }

    // This test will only run on machines where the user has added a entry for proxy.http
    @Test
    public void testHTTPProxy() throws IOException, URISyntaxException {
        // Unset proxy before testing
        System.setProperty("http.proxyHost", "");
        System.setProperty("https.proxyHost", "");
        System.setProperty("socksProxyHost", "");
        URL url = new URI("https://icanhazip.com").toURL();
        String proxyConfig = Utils.getConfigString("proxy.http", "");
        if (!proxyConfig.equals("")) {
            String ip1 = Http.url(url).ignoreContentType().get().text();
            Proxy.setHTTPProxy(Utils.getConfigString("proxy.http", ""));
            String ip2 = Http.url(url).ignoreContentType().get().text();
            assertFalse(ip1.equals(ip2));
        } else {
            System.out.println("Skipping testHTTPProxy");
            assert(true);
        }
    }

}
