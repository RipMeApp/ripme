package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.ChanRipper;

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
        passURLs.add(new URL("https://xchan.pw/board/porn/thread/874116/"));
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
        // URLs that should return more than 1 image
        //contentURLs.add(new URL("http://desuchan.net/v/res/7034.html"));
        //contentURLs.add(new URL("http://boards.420chan.org/ana/res/75984.php"));
        //contentURLs.add(new URL("http://archive.4plebs.org/s4s/thread/3005257/"));
        //contentURLs.add(new URL("http://drawchan.net/dc/dw/res/114910.html"));

        // Most *chans have volatile threads & can't be trusted for integration testing.

        //contentURLs.add(new URL("http://boards.4chan.org/r/res/12225949"));
        //contentURLs.add(new URL("http://7chan.org/gif/res/23795.html"));
        //contentURLs.add(new URL("http://unichan2.org/b/res/518004.html"));

        // xchan has an HTTPS certificaiton error...
        //contentURLs.add(new URL("http://xchan.pw/porn/res/437.html"));
        for (URL url : contentURLs) {
            ChanRipper ripper = new ChanRipper(url);
            testRipper(ripper);
        }
    }

}
