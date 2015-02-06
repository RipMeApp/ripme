package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.MotherlessRipper;

public class MotherlessRipperTest extends RippersTest {
    
    public void testMotherlessAlbums() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();

        // Image album
        contentURLs.add(new URL("http://motherless.com/G4DAA18D"));
        // Video album
        // XXX: Commented out because test takes too long to download the file.
        // contentURLs.add(new URL("http://motherless.com/GFD0F537"));

        for (URL url : contentURLs) {
            MotherlessRipper ripper = new MotherlessRipper(url);
            testRipper(ripper);
        }
    }

}
