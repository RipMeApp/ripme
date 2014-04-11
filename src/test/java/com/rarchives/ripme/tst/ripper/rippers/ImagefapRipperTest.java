package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rarchives.ripme.ripper.rippers.ImagefapRipper;

public class ImagefapRipperTest extends RippersTest {
    
    public void testImagefapGID() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        Map<URL, String> testURLs = new HashMap<URL, String>();
        testURLs.put(new URL("http://www.imagefap.com/pictures/4649440/Frozen-%28Elsa-and-Anna%29?view=2"), "Frozen (Elsa and Anna)");
        for (URL url : testURLs.keySet()) {
            ImagefapRipper ripper = new ImagefapRipper(url);
            assertEquals(testURLs.get(url), ripper.getAlbumTitle(ripper.getURL()));
            deleteDir(ripper.getWorkingDir());
        }
    }

    public void testImagefapAlbums() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://www.imagefap.com/pictures/4649440/Frozen-%28Elsa-and-Anna%29?view=2"));
        for (URL url : contentURLs) {
            try {
                ImagefapRipper ripper = new ImagefapRipper(url);
                ripper.rip();
                assert(ripper.getWorkingDir().listFiles().length > 1);
                deleteDir(ripper.getWorkingDir());
            } catch (Exception e) {
                fail("Error while ripping URL " + url + ": " + e.getMessage());
            }
        }
    }

}
