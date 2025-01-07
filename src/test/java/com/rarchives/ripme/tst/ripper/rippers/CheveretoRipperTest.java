package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.rippers.CheveretoRipper;

public class CheveretoRipperTest extends RippersTest {
    @Test
    public void testSubdirAlbum1() throws IOException, URISyntaxException {
        CheveretoRipper ripper = new CheveretoRipper(new URI("https://kenzato.uk/booru/album/TnEc").toURL());
        testRipper(ripper);
    }

    @Test
    public void testSubdirAlbum2() throws IOException, URISyntaxException {
        CheveretoRipper ripper = new CheveretoRipper(new URI("https://kenzato.uk/booru/album/XWdIp").toURL());
        testRipper(ripper);
    }
}
