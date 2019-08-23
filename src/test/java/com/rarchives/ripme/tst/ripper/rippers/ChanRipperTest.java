package com.rarchives.ripme.tst.ripper.rippers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.ChanRipper;
import com.rarchives.ripme.ripper.rippers.ripperhelpers.ChanSite;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

public class ChanRipperTest extends RippersTest {
    @Test
    public void testChanURLFailures() throws IOException {
        List<URL> failURLs = new ArrayList<>();
        // URLs that should not work
        for (URL url : failURLs) {
            try {
                new ChanRipper(url);
                fail("Instantiated ripper for URL that should not work: " + url);
            } catch (Exception e) {
                // Expected
            }
        }
    }
    @Test
    public void testChanURLPasses() throws IOException {
        List<URL> passURLs = new ArrayList<>();
        // URLs that should work
        passURLs.add(new URL("http://desuchan.net/v/res/7034.html"));
        passURLs.add(new URL("https://boards.4chan.org/hr/thread/3015701"));
        passURLs.add(new URL("https://boards.420chan.org/420/res/232066.php"));
        passURLs.add(new URL("http://7chan.org/gif/res/25873.html"));
        passURLs.add(new URL("https://rbt.asia/g/thread/70643087/")); //must work with TLDs with len of 4
        for (URL url : passURLs) {
            ChanRipper ripper = new ChanRipper(url);
            ripper.setup();
            assert (ripper.canRip(url));
            assertNotNull("Ripper for " + url + " did not have a valid working directory.", ripper.getWorkingDir());
            deleteDir(ripper.getWorkingDir());
        }
    }
    @Test
    public void testChanStringParsing() throws IOException {
        List<String> site1 = Arrays.asList("site1.com");
        List<String> site1Cdns = Arrays.asList("cnd1.site1.com", "cdn2.site2.biz");

        List<String> site2 = Arrays.asList("site2.co.uk");
        List<String> site2Cdns = Arrays.asList("cdn.site2.co.uk");
        ChanRipper ripper = new ChanRipper(new URL("http://desuchan.net/v/res/7034.html"));
        List<ChanSite> chansFromConfig = ripper
                .getChansFromConfig("site1.com[cnd1.site1.com|cdn2.site2.biz],site2.co.uk[cdn.site2.co.uk]");
        assertEquals(chansFromConfig.get(0).getDomains(), site1);
        assertEquals(chansFromConfig.get(0).getCdns(), site1Cdns);

        assertEquals(chansFromConfig.get(1).getDomains(), site2);
        assertEquals(chansFromConfig.get(1).getCdns(), site2Cdns);
    }
    @Test
    public void testChanRipper() throws IOException {
        List<URL> contentURLs = new ArrayList<>();
        contentURLs.add(new URL(getRandomThreadDesuarchive()));
        for (URL url : contentURLs) {
            ChanRipper ripper = new ChanRipper(url);
            testChanRipper(ripper);
        }
    }

    /**
     *
     * @return String returns a url to a active desuarchive.org tread as a string
     */
    public String getRandomThreadDesuarchive() {
        try {
            Document doc = Http.url(new URL("https://desuarchive.org/wsg/")).get();
            return doc.select("div.post_data > a").first().attr("href");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
