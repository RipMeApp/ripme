package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.CheveretoRipper;
import org.junit.jupiter.api.Test;

public class CheveretoRipperTest extends RippersTest {
    @Test
    public void testTagFox() throws IOException {
        CheveretoRipper ripper = new CheveretoRipper(new URL("http://tag-fox.com/album/Thjb"));
        testRipper(ripper);
    }
    @Test
    public void testSubdirAlbum() throws IOException {
        CheveretoRipper ripper = new CheveretoRipper(new URL("https://kenzato.uk/booru/album/TnEc"));
        testRipper(ripper);
    }
}
