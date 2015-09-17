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

    public void testImgurAlbums() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();
        // URLs that should return more than 1 image
        contentURLs.add(new URL("http://imgur.com/a/dS9OQ#0")); // Horizontal layout
        contentURLs.add(new URL("http://imgur.com/a/YpsW9#0")); // Grid layout
        contentURLs.add(new URL("http://imgur.com/a/WxG6f/layout/vertical#0"));
        contentURLs.add(new URL("http://imgur.com/a/WxG6f/layout/horizontal#0"));
        contentURLs.add(new URL("http://imgur.com/a/WxG6f/layout/grid#0"));
        // Sometimes hangs up
        //contentURLs.add(new URL("http://imgur.com/r/nsfw_oc/top/all"));
        contentURLs.add(new URL("http://imgur.com/a/bXQpH"));
        for (URL url : contentURLs) {
            ImgurRipper ripper = new ImgurRipper(url);
            testRipper(ripper);
        }
    }

}
