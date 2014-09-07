package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.ChanRipper;

public class ChanRipperTest extends RippersTest {

    public void testChanURLFailures() throws IOException {
        List<URL> failURLs = new ArrayList<URL>();
        // URLs that should not work
        for (URL url : failURLs) {
            try {
                new ChanRipper(url);
                fail("Instantiated ripper for URL that should not work: " + url);
            } catch (Exception e) {
                // Expected
                continue;
            }
        }
    }

    public void testChanURLPasses() throws IOException {
        List<URL> passURLs    = new ArrayList<URL>();
        // URLs that should work
        passURLs.add(new URL("http://desuchan.net/v/res/7034.html"));
        passURLs.add(new URL("http://boards.4chan.org/hr/thread/2214511"));
        passURLs.add(new URL("http://fgts.jp/r/thread/12225949/"));
        passURLs.add(new URL("http://boards.420chan.org/ana/res/75984.php"));
        passURLs.add(new URL("http://7chan.org/gif/res/23795.html"));
        passURLs.add(new URL("http://unichan2.org/b/res/518004.html"));
        passURLs.add(new URL("http://xchan.pw/porn/res/437.html"));
        passURLs.add(new URL("http://archive.moe/c/thread/2295132/"));
        for (URL url : passURLs) {
            try {
                ChanRipper ripper = new ChanRipper(url);
                assert(ripper.canRip(url));
                deleteDir(ripper.getWorkingDir());
            } catch (Exception e) {
                fail("Failed to instantiate ripper for " + url + " with message: "+e.toString());
            }
        }
    }

    public void testChanRipper() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();
        // URLs that should return more than 1 image
        contentURLs.add(new URL("http://desuchan.net/v/res/7034.html"));
        contentURLs.add(new URL("http://boards.4chan.org/r/res/12225949"));
        contentURLs.add(new URL("http://boards.420chan.org/ana/res/75984.php"));
        contentURLs.add(new URL("http://7chan.org/gif/res/23795.html"));
        contentURLs.add(new URL("http://unichan2.org/b/res/518004.html"));
        contentURLs.add(new URL("http://xchan.pw/porn/res/437.html"));
        contentURLs.add(new URL("http://archive.4plebs.org/hr/thread/2215899/"));
        for (URL url : contentURLs) {
            try {
                ChanRipper ripper = new ChanRipper(url);
                ripper.rip();
                assert(ripper.getWorkingDir().listFiles().length > 1);
                deleteDir(ripper.getWorkingDir());
            } catch (Exception e) {
                e.printStackTrace();
                fail("Error while ripping URL " + url + ": " + e.getMessage());
            }
        }
    }

}
