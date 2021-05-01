package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ZizkiRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class ZizkiRipperTest extends RippersTest {

    @Test
    @Tag("flaky")
    public void testRip() throws IOException {
        ZizkiRipper ripper = new ZizkiRipper(new URL("http://zizki.com/dee-chorde/we-got-spirit"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("http://zizki.com/dee-chorde/we-got-spirit");
        ZizkiRipper ripper = new ZizkiRipper(url);
        Assertions.assertEquals("dee-chorde", ripper.getGID(url));
    }

    @Test
    @Tag("flaky")
    public void testAlbumTitle() throws IOException {
        URL url = new URL("http://zizki.com/dee-chorde/we-got-spirit");
        ZizkiRipper ripper = new ZizkiRipper(url);
        Assertions.assertEquals("zizki_Dee Chorde_We Got Spirit", ripper.getAlbumTitle(url));
    }
}
