package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.ImgurRipper;

public class ImgurRipperTest extends RippersTest {

    public void testImgurURLFailures() throws IOException {
        List<URL> failURLs = new ArrayList<URL>();
        // Imgur urls that should not work
        failURLs.add(new URL("http://imgur.com"));
        failURLs.add(new URL("http://imgur.com/"));
        failURLs.add(new URL("http://i.imgur.com"));
        failURLs.add(new URL("http://i.imgur.com/"));
        failURLs.add(new URL("http://imgur.com/image"));
        failURLs.add(new URL("http://imgur.com/image.jpg"));
        failURLs.add(new URL("http://i.imgur.com/image.jpg"));
        for (URL url : failURLs) {
            try {
                new ImgurRipper(url);
                fail("Instantiated ripper for URL that should not work: " + url);
            } catch (Exception e) {
                // Expected
                continue;
            }
        }
    }

    public void testImgurURLPasses() throws IOException {
        List<URL> passURLs    = new ArrayList<URL>();
        // Imgur URLs that should work
        passURLs.add(new URL("http://imgur.com/a/XPd4F"));
        passURLs.add(new URL("http://imgur.com/a/XPd4F/"));
        passURLs.add(new URL("http://imgur.com/a/WxG6f/all"));
        passURLs.add(new URL("http://imgur.com/a/WxG6f/layout/vertical#0"));
        passURLs.add(new URL("http://imgur.com/a/WxG6f/layout/horizontal#0"));
        passURLs.add(new URL("http://imgur.com/a/WxG6f/layout/grid#0"));
        passURLs.add(new URL("http://imgur.com/YOdjht3,x5VxH9G,5juXjJ2"));
        passURLs.add(new URL("http://markedone911.imgur.com"));
        passURLs.add(new URL("http://markedone911.imgur.com/"));

        for (URL url : passURLs) {
            try {
                ImgurRipper ripper = new ImgurRipper(url);
                assertTrue(ripper.canRip(url));
                deleteDir(ripper.getWorkingDir());
            } catch (Exception e) {
                fail("Failed to instantiate ripper for " + url);
            }
        }
    }

    public void testImgurAlbums() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();
        // URLs that should return more than 1 image
        contentURLs.add(new URL("http://imgur.com/a/hqJIu")); // Vertical layout
        contentURLs.add(new URL("http://imgur.com/a/dS9OQ#0")); // Horizontal layout
        contentURLs.add(new URL("http://imgur.com/a/YpsW9#0")); // Grid layout
        contentURLs.add(new URL("http://imgur.com/a/WxG6f/layout/vertical#0"));
        contentURLs.add(new URL("http://imgur.com/a/WxG6f/layout/horizontal#0"));
        contentURLs.add(new URL("http://imgur.com/a/WxG6f/layout/grid#0"));
        for (URL url : contentURLs) {
            try {
                ImgurRipper ripper = new ImgurRipper(url);
                ripper.rip();
                assert(ripper.getWorkingDir().listFiles().length > 1);
                deleteDir(ripper.getWorkingDir());
            } catch (Exception e) {
                fail("Error while ripping URL " + url + ": " + e.getMessage());
            }
        }
    }

}
