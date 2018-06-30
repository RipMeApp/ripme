package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.DeviantartRipper;

public class DeviantartRipperTest extends RippersTest {
    public void testDeviantartAlbum() throws IOException {
        DeviantartRipper ripper = new DeviantartRipper(new URL("https://www.deviantart.com/airgee/gallery/"));
        testRipper(ripper);
    }

    public void testDeviantartNSFWAlbum() throws IOException {
        // NSFW gallery
        DeviantartRipper ripper = new DeviantartRipper(new URL("https://www.deviantart.com/faterkcx/gallery/"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://www.deviantart.com/airgee/gallery/");
        DeviantartRipper ripper = new DeviantartRipper(url);
        assertEquals("airgee", ripper.getGID(url));
    }
}
