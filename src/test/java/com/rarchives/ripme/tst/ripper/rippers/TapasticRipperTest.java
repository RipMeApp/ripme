package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.TapasticRipper;

public class TapasticRipperTest extends RippersTest {
    public void testTapasticRip() throws IOException {
        TapasticRipper ripper = new TapasticRipper(new URL("https://tapas.io/series/tsiwbakd-comic"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://tapas.io/series/tsiwbakd-comic");
        TapasticRipper ripper = new TapasticRipper(url);
        assertEquals("series_ tsiwbakd-comic", ripper.getGID(url));
    }
}
