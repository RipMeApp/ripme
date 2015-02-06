package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.DeviantartRipper;

public class DeviantartRipperTest extends RippersTest {

    public void testDeviantartAlbums() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();

        // Small gallery
        contentURLs.add(new URL("http://airgee.deviantart.com/gallery/"));
        // NSFW gallery
        contentURLs.add(new URL("http://faterkcx.deviantart.com/gallery/"));
        // Multi-page NSFW
        contentURLs.add(new URL("http://geekysica.deviantart.com/gallery/35209412"));

        for (URL url : contentURLs) {
            DeviantartRipper ripper = new DeviantartRipper(url);
            testRipper(ripper);
        }
    }

}
