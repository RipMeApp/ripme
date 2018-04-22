package com.rarchives.ripme.tst;

import java.io.IOException;
import java.net.URL;
import com.rarchives.ripme.utils.Proxy;
import com.rarchives.ripme.utils.Utils;
import junit.framework.TestCase;
import com.rarchives.ripme.utils.Http;


public class proxyTest  extends TestCase {


    // This test will only run on machines where the user has added a entry for proxy.socks
    public void testSocksProxy() throws IOException {
        // Unset proxy before testing
        System.setProperty("http.proxyHost", "");
        System.setProperty("https.proxyHost", "");
        System.setProperty("socksProxyHost", "");
        URL url = new URL("https://icanhazip.com");
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
    public void testHTTPProxy() throws IOException {
        // Unset proxy before testing
        System.setProperty("http.proxyHost", "");
        System.setProperty("https.proxyHost", "");
        System.setProperty("socksProxyHost", "");
        URL url = new URL("https://icanhazip.com");
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
