//package com.rarchives.ripme.tst.ripper.rippers;
//
//import java.io.IOException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//
//import com.rarchives.ripme.ripper.rippers.NatalieMuRipper;
//
//public class NatalieMuRipperTest extends RippersTest {
//
//    public void testNatalieMuURLFailures() throws IOException {
//        List<URL> failURLs = new ArrayList<>();
//        // URLs that should not work
//        for (URL url : failURLs) {
//            try {
//                new NatalieMuRipper(url);
//                fail("Instantiated ripper for URL that should not work: " + url);
//            } catch (Exception e) {
//                // Expected
//            }
//        }
//    }
//
//    public void testNatalieMuURLPasses() throws IOException {
//        List<URL> passURLs    = new ArrayList<>();
//        // URLs that should work
//        passURLs.add(new URI("http://natalie.mu/music/news/140367").toURL());
//        passURLs.add(new URI("http://cdn2.natalie.mu/music/news/140411").toURL());
//        passURLs.add(new URI("http://cdn2.natalie.mu/music/gallery/show/news_id/140411/image_id/369655").toURL());
//        passURLs.add(new URI("http://natalie.mu/music/gallery/show/news_id/139146/image_id/365218").toURL());
//        for (URL url : passURLs) {
//            NatalieMuRipper ripper = new NatalieMuRipper(url);
//            ripper.setup();
//            assert(ripper.canRip(url));
//            assertNotNull("Ripper for " + url + " did not have a valid working directory.",
//                          ripper.getWorkingDir());
//            deleteDir(ripper.getWorkingDir());
//        }
//    }
//
//    public void testNatalieMuRipper() throws IOException {
//        List<URL> contentURLs = new ArrayList<>();
//        // URLs that should return more than 1 image
//        contentURLs.add(new URI("http://natalie.mu/music/news/140367").toURL());
//        contentURLs.add(new URI("http://cdn2.natalie.mu/music/news/140411").toURL());
//        contentURLs.add(new URI("http://cdn2.natalie.mu/music/gallery/show/news_id/140411/image_id/369655").toURL());
//        contentURLs.add(new URI("http://natalie.mu/music/gallery/show/news_id/139146/image_id/365218").toURL());
//
//        // Most *chans have volatile threads & can't be trusted for integration testing.
//
//        //contentURLs.add(new URI("http://boards.4chan.org/r/res/12225949").toURL());
//        //contentURLs.add(new URI("http://7chan.org/gif/res/23795.html").toURL());
//        //contentURLs.add(new URI("http://unichan2.org/b/res/518004.html").toURL());
//
//        // xchan has an HTTPS certificaiton error...
//        //contentURLs.add(new URI("http://xchan.pw/porn/res/437.html").toURL());
//        for (URL url : contentURLs) {
//            NatalieMuRipper ripper = new NatalieMuRipper(url);
//            testRipper(ripper);
//        }
//    }
//
//}
