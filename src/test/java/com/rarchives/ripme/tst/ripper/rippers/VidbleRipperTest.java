package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.VidbleRipper;
<<<<<<< HEAD
=======
import org.junit.jupiter.api.Assertions;
>>>>>>> upstream/master
import org.junit.jupiter.api.Test;

public class VidbleRipperTest extends RippersTest {
    @Test
    public void testVidbleRip() throws IOException {
        VidbleRipper ripper = new VidbleRipper(new URL("http://www.vidble.com/album/y1oyh3zd"));
        testRipper(ripper);
    }

<<<<<<< HEAD
    public void testGetGID() throws IOException {
        URL url = new URL("http://www.vidble.com/album/y1oyh3zd");
        VidbleRipper ripper = new VidbleRipper(url);
        assertEquals("y1oyh3zd", ripper.getGID(url));
=======
    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("http://www.vidble.com/album/y1oyh3zd");
        VidbleRipper ripper = new VidbleRipper(url);
        Assertions.assertEquals("y1oyh3zd", ripper.getGID(url));
>>>>>>> upstream/master
    }
}

