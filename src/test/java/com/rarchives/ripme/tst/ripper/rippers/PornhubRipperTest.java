package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.PornhubRipper;

public class PornhubRipperTest extends RippersTest {
    public void testPornhubRip() throws IOException {
        PornhubRipper ripper = new PornhubRipper(new URL("https://www.pornhub.com/album/15680522"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://www.pornhub.com/album/15680522");
        PornhubRipper ripper = new PornhubRipper(url);
        assertEquals("15680522", ripper.getGID(url));
    }
}
