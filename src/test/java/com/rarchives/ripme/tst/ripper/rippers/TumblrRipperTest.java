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
        contentURLs.add(new URL("http://fittingroomgirls.tumblr.com/post/78268776776"));
        for (URL url : contentURLs) {
            try {
                TumblrRipper ripper = new TumblrRipper(url);
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
