package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.XhamsterRipper;

public class XhamsterRipperTest extends RippersTest {
    
    public void testXhamsterAlbums() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://xhamster.com/photos/gallery/1462237/alyssa_gadson.html"));
        contentURLs.add(new URL("http://xhamster.com/photos/gallery/2941201/tableau_d_039_art_ii.html"));
        for (URL url : contentURLs) {
            try {
                XhamsterRipper ripper = new XhamsterRipper(url);
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
