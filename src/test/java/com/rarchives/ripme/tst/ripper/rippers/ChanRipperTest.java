package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.rarchives.ripme.ripper.rippers.ChanRipper;
import com.rarchives.ripme.ripper.rippers.ripperhelpers.ChanSite;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChanRipperTest extends RippersTest {
    @Test
    public void testChanURLPasses() throws IOException, URISyntaxException {
        List<URL> passURLs = new ArrayList<>();
        // URLs that should work
        passURLs.add(new URI("http://desuchan.net/v/res/7034.html").toURL());
        passURLs.add(new URI("https://boards.4chan.org/hr/thread/3015701").toURL());
        passURLs.add(new URI("https://boards.420chan.org/420/res/232066.php").toURL());
        passURLs.add(new URI("http://7chan.org/gif/res/25873.html").toURL());
        passURLs.add(new URI("https://rbt.asia/g/thread/70643087/").toURL()); //must work with TLDs with len of 4
        for (URL url : passURLs) {
            ChanRipper ripper = new ChanRipper(url);
            // Use CompletableFuture to run setup() asynchronously
            CompletableFuture<Void> setupFuture = CompletableFuture.runAsync(() -> {
                try {
                    ripper.setup();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            try {
                // Wait for up to 5 seconds for setup() to complete
                setupFuture.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException |
                     TimeoutException e) {
                e.printStackTrace(); // Handle exceptions as needed
            }
            assert (ripper.canRip(url));
            Assertions.assertNotNull(ripper.getWorkingDir(), "Ripper for " + url + " did not have a valid working directory.");
            deleteDir(ripper.getWorkingDir());
        }
    }
    @Test
    public void testChanStringParsing() throws IOException, URISyntaxException {
        List<String> site1 = Arrays.asList("site1.com");
        List<String> site1Cdns = Arrays.asList("cnd1.site1.com", "cdn2.site2.biz");

        List<String> site2 = Arrays.asList("site2.co.uk");
        List<String> site2Cdns = Arrays.asList("cdn.site2.co.uk");
        ChanRipper ripper = new ChanRipper(new URI("http://desuchan.net/v/res/7034.html").toURL());
        List<ChanSite> chansFromConfig = ripper
                .getChansFromConfig("site1.com[cnd1.site1.com|cdn2.site2.biz],site2.co.uk[cdn.site2.co.uk]");
        Assertions.assertEquals(chansFromConfig.get(0).getDomains(), site1);
        Assertions.assertEquals(chansFromConfig.get(0).getCdns(), site1Cdns);

        Assertions.assertEquals(chansFromConfig.get(1).getDomains(), site2);
        Assertions.assertEquals(chansFromConfig.get(1).getCdns(), site2Cdns);
    }
    @Test
    public void testChanRipper() throws IOException, URISyntaxException {
        List<URL> contentURLs = new ArrayList<>();
        contentURLs.add(getRandomThreadDesuarchive());
        for (URL url : contentURLs) {
            ChanRipper ripper = new ChanRipper(url);
            testChanRipper(ripper);
        }
    }

    public URL getRandomThreadDesuarchive() throws URISyntaxException {
        try {
            Document doc = Http.url(new URI("https://desuarchive.org/wsg/").toURL()).get();
            return new URI(doc.select("div.post_data > a").first().attr("href")).toURL();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
