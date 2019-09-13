package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.FuskatorRipper;
import org.junit.jupiter.api.Test;

public class FuskatorRipperTest extends RippersTest {
    @Test
    public void testFuskatorAlbum() throws IOException {
        FuskatorRipper ripper = new FuskatorRipper(new URL("https://fuskator.com/thumbs/hqt6pPXAf9z/Shaved-Blonde-Babe-Katerina-Ambre.html"));
        testRipper(ripper);
    }
    @Test
    public void testUrlsWithTiled() throws IOException {
        FuskatorRipper ripper = new FuskatorRipper(new URL("https://fuskator.com/thumbs/hsrzk~UIFmJ/Blonde-Babe-Destiny-Dixon-Playing-With-Black-Dildo.html"));
        testRipper(ripper);
    }
}
