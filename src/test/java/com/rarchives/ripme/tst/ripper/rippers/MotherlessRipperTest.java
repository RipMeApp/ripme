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
        contentURLs.add(new URL("http://motherless.com/GFD0F537"));

        for (URL url : contentURLs) {
            try {
                MotherlessRipper ripper = new MotherlessRipper(url);
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
