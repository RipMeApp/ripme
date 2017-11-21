package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.DeviantartRipper;

public class DeviantartRipperTest extends RippersTest {
    public void testDeviantartAlbum() throws IOException {
        DeviantartRipper ripper = new DeviantartRipper(new URL("http://airgee.deviantart.com/gallery/"));
        testRipper(ripper);
    }

    public void testDeviantartNSFWAlbum() throws IOException {
        // NSFW gallery
        DeviantartRipper ripper = new DeviantartRipper(new URL("http://faterkcx.deviantart.com/gallery/"));
        testRipper(ripper);
    }
}
