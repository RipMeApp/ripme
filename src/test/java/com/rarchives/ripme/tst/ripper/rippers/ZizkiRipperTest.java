package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ZizkiRipper;

public class ZizkiRipperTest extends RippersTest {
    public void testRip() throws IOException {
        ZizkiRipper ripper = new ZizkiRipper(new URL("http://zizki.com/dee-chorde/we-got-spirit"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("http://zizki.com/dee-chorde/we-got-spirit");
        ZizkiRipper ripper = new ZizkiRipper(url);
        assertEquals("dee-chorde", ripper.getGID(url));
    }

    public void testAlbumTitle() throws IOException {
        URL url = new URL("http://zizki.com/dee-chorde/we-got-spirit");
        ZizkiRipper ripper = new ZizkiRipper(url);
        assertEquals("zizki_Dee Chorde_We Got Spirit", ripper.getAlbumTitle(url));
    }
}
