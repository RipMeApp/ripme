package com.rarchives.ripme.tst;

import java.io.IOException;
import java.net.URL;
import com.rarchives.ripme.utils.Utils;
import junit.framework.TestCase;
import com.rarchives.ripme.utils.Http;


public class proxyTest  extends TestCase {
    // This test will only run on machines where the user has added a entry for socks.proxy
    public void testSocksProxy() throws IOException {
        URL url = new URL("https://icanhazip.com");
        String proxyConfig = Utils.getConfigString("socks.proxy", "");
        if (!proxyConfig.equals("")) {
            String ip1 = Http.url(url).ignoreContentType().get().text();
            Utils.setProxy(Utils.getConfigString("socks.proxy", ""));
            String ip2 = Http.url(url).ignoreContentType().get().text();
            assertFalse(ip1.equals(ip2));
        } else {
            System.out.println("SKipping proxyTest");
            assert(true);
        }
    }

}
