package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.TwitterRipper;

public class TwitterRipperTest extends RippersTest {

    public void testTwitterAlbums() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();
        //contentURLs.add(new URL("https://twitter.com/danngamber01/media"));
        contentURLs.add(new URL("https://twitter.com/search?q=from%3Apurrbunny%20filter%3Aimages&src=typd"));
        for (URL url : contentURLs) {
            try {
                TwitterRipper ripper = new TwitterRipper(url);
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
