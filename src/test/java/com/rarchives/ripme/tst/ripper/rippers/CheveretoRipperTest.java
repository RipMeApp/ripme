package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.CheveretoRipper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class CheveretoRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testTagFox() throws IOException, URISyntaxException {
        CheveretoRipper ripper = new CheveretoRipper(new URI("http://tag-fox.com/album/Thjb").toURL());
        testRipper(ripper);
    }
    @Test
    @Tag("flaky")
    public void testSubdirAlbum() throws IOException, URISyntaxException {
        CheveretoRipper ripper = new CheveretoRipper(new URI("https://kenzato.uk/booru/album/TnEc").toURL());
        testRipper(ripper);
    }
}
