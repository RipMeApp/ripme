package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.GonewildRipper;

public class GonewildRipperTest extends RippersTest {
    
    public void testInstagramAlbums() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://gonewild.com/user/amle69"));
        for (URL url : contentURLs) {
            GonewildRipper ripper = new GonewildRipper(url);
            testRipper(ripper);
        }
    }

}
