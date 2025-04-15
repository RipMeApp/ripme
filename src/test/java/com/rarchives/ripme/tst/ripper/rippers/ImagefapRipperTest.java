package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
    public void testImagefapAlbums() throws IOException, URISyntaxException {
        Map<URL, String> testURLs = new HashMap<>();

        // Album with specific title
        testURLs.put(new URI("https://www.imagefap.com/pictures/11365460/Cartoons").toURL(),
                             "Cartoons");

        for (URL url : testURLs.keySet()) {
            ImagefapRipper ripper = new ImagefapRipper(url);
            testRipper(ripper);
        }
    }
    @Test
    @Tag("flaky")
    public void testImagefapGetAlbumTitle() throws IOException, URISyntaxException {
        URL url = new URI("https://www.imagefap.com/pictures/11365460/Cartoons").toURL();
        ImagefapRipper ripper = new ImagefapRipper(url);
        Assertions.assertEquals("imagefap_Cartoons_11365460", ripper.getAlbumTitle(url));
    }
}
