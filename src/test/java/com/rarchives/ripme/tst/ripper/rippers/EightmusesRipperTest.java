package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.EightmusesRipper;

public class EightmusesRipperTest extends RippersTest {
    
    public void testEightmusesAlbums() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();

        contentURLs.add(new URL("http://www.8muses.com/index/category/jab-hotassneighbor7"));

        for (URL url : contentURLs) {
            try {
                EightmusesRipper ripper = new EightmusesRipper(url);
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
