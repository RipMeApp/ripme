package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.VidbleRipper;

public class VidbleRipperTest extends RippersTest {
    public void testVidbleRip() throws IOException {
        VidbleRipper ripper = new VidbleRipper(new URL("http://www.vidble.com/album/y1oyh3zd"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("http://www.vidble.com/album/y1oyh3zd");
        VidbleRipper ripper = new VidbleRipper(url);
        assertEquals("y1oyh3zd", ripper.getGID(url));
    }
}

