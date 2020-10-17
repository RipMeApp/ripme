package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ZizkiRipper;
<<<<<<< HEAD
=======
import org.junit.jupiter.api.Assertions;
>>>>>>> upstream/master
import org.junit.jupiter.api.Test;

public class ZizkiRipperTest extends RippersTest {
    public void testRip() throws IOException {
        ZizkiRipper ripper = new ZizkiRipper(new URL("http://zizki.com/dee-chorde/we-got-spirit"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("http://zizki.com/dee-chorde/we-got-spirit");
        ZizkiRipper ripper = new ZizkiRipper(url);
<<<<<<< HEAD
        assertEquals("dee-chorde", ripper.getGID(url));
=======
        Assertions.assertEquals("dee-chorde", ripper.getGID(url));
>>>>>>> upstream/master
    }
    @Test
    public void testAlbumTitle() throws IOException {
        URL url = new URL("http://zizki.com/dee-chorde/we-got-spirit");
        ZizkiRipper ripper = new ZizkiRipper(url);
<<<<<<< HEAD
        assertEquals("zizki_Dee Chorde_We Got Spirit", ripper.getAlbumTitle(url));
=======
        Assertions.assertEquals("zizki_Dee Chorde_We Got Spirit", ripper.getAlbumTitle(url));
>>>>>>> upstream/master
    }
}
