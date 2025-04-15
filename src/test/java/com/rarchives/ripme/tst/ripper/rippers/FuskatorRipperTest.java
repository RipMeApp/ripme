package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.FuskatorRipper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FuskatorRipperTest extends RippersTest {
    @Test
    @Disabled("test or ripper broken")
    public void testFuskatorAlbum() throws IOException, URISyntaxException {
        FuskatorRipper ripper = new FuskatorRipper(new URI("https://fuskator.com/thumbs/hqt6pPXAf9z/Shaved-Blonde-Babe-Katerina-Ambre.html").toURL());
        testRipper(ripper);
    }
    @Test
    @Disabled("test or ripper broken")
    public void testUrlsWithTiled() throws IOException, URISyntaxException {
        FuskatorRipper ripper = new FuskatorRipper(new URI("https://fuskator.com/thumbs/hsrzk~UIFmJ/Blonde-Babe-Destiny-Dixon-Playing-With-Black-Dildo.html").toURL());
        testRipper(ripper);
    }
}
