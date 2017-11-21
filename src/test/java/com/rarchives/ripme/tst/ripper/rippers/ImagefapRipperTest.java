package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.rarchives.ripme.ripper.rippers.ImagefapRipper;

public class ImagefapRipperTest extends RippersTest {
    public void testImagefapAlbums() throws IOException {
        Map<URL, String> testURLs = new HashMap<>();

        // Album with specific title
        testURLs.put(new URL("http://www.imagefap.com/pictures/4649440/Frozen-%28Elsa-and-Anna%29?view=2"),
                             "Frozen (Elsa and Anna)");

        // New URL format
        testURLs.put(new URL("http://www.imagefap.com/gallery.php?pgid=fffd68f659befa5535cf78f014e348f1"),
                             "imagefap_fffd68f659befa5535cf78f014e348f1");

        for (URL url : testURLs.keySet()) {
            ImagefapRipper ripper = new ImagefapRipper(url);
            testRipper(ripper);
        }
    }
}
