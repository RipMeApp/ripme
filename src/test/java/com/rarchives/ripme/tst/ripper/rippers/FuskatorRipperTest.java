package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.FuskatorRipper;

public class FuskatorRipperTest extends RippersTest {
    public void testFuskatorAlbum() throws IOException {
        FuskatorRipper ripper = new FuskatorRipper(new URL("https://fuskator.com/thumbs/hqt6pPXAf9z/Shaved-Blonde-Babe-Katerina-Ambre.html"));
        testRipper(ripper);
    }
    
    public void testUrlsWithTiled() throws IOException {
        FuskatorRipper ripper = new FuskatorRipper(new URL("https://fuskator.com/thumbs/hsrzk~UIFmJ/Blonde-Babe-Destiny-Dixon-Playing-With-Black-Dildo.html"));
        testRipper(ripper);
    }
}
