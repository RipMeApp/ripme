package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.TapasticRipper;

public class TapasRipperTest extends RippersTest {
    public void testTapasRipperAlbum() throws IOException {
        TapasticRipper ripper = new TapasticRipper(new URL("https://tapas.io/series/tsiwbakd-comic"));
        testRipper(ripper);
    }
}