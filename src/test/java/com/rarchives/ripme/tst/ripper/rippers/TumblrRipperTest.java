package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.TumblrRipper;

public class TumblrRipperTest extends RippersTest {
    
    public void testTumblrAlbums() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://wrouinr.tumblr.com/archive"));
        contentURLs.add(new URL("http://topinstagirls.tumblr.com/tagged/berlinskaya"));
        contentURLs.add(new URL("http://genekellyclarkson.tumblr.com/post/86100752527/lucyannebrooks-rachaelboden-friends-goodtimes-bed-boobs"));
        for (URL url : contentURLs) {
            TumblrRipper ripper = new TumblrRipper(url);
            testRipper(ripper);
        }
    }

}
