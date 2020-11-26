package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.rarchives.ripme.ripper.rippers.ImagefapRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class ImagefapRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
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
    @Test
    @Tag("flaky")
    public void testImagefapGetAlbumTitle() throws IOException {
        URL url = new URL("https://www.imagefap.com/gallery.php?gid=7789753");
        ImagefapRipper ripper = new ImagefapRipper(url);
        Assertions.assertEquals("imagefap_Red.Heels.Lover.In.Love_7789753", ripper.getAlbumTitle(url));
    }
}
