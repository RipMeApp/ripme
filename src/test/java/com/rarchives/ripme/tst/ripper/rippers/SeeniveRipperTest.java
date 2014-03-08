package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.SeeniveRipper;

public class SeeniveRipperTest extends RippersTest {
    
    public void testSeeniveAlbums() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://seenive.com/u/946491170220040192"));
        for (URL url : contentURLs) {
            try {
                SeeniveRipper ripper = new SeeniveRipper(url);
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
