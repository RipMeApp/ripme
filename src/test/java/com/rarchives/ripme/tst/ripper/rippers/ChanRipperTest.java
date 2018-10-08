package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.ChanRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;

public class ChanRipperTest extends RippersTest {

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

    public void testChanURLPasses() throws IOException {
        List<URL> passURLs    = new ArrayList<>();
        // URLs that should work
        passURLs.add(new URL("http://desuchan.net/v/res/7034.html"));
        passURLs.add(new URL("https://boards.4chan.org/hr/thread/3015701"));
        passURLs.add(new URL("https://boards.420chan.org/420/res/232066.php"));
        passURLs.add(new URL("http://7chan.org/gif/res/25873.html"));
        for (URL url : passURLs) {
            ChanRipper ripper = new ChanRipper(url);
            ripper.setup();
            assert(ripper.canRip(url));
            assertNotNull("Ripper for " + url + " did not have a valid working directory.",
                          ripper.getWorkingDir());
            deleteDir(ripper.getWorkingDir());
        }
    }

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
            System.out.println(doc);
            return doc.select("div.post_data > a").first().attr("href");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
