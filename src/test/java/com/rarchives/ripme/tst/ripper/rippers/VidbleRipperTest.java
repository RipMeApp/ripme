package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.VidbleRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VidbleRipperTest extends RippersTest {
    @Test
    public void testVidbleRip() throws IOException, URISyntaxException {
        VidbleRipper ripper = new VidbleRipper(new URI("https://vidble.com/album/cGEFr8zi").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://vidble.com/album/cGEFr8zi").toURL();
        VidbleRipper ripper = new VidbleRipper(url);
        Assertions.assertEquals("cGEFr8zi", ripper.getGID(url));
    }
}

