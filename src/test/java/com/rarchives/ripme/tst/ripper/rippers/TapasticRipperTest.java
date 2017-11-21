package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.TapasticRipper;

public class TapasticRipperTest extends RippersTest {
    public void testTapasticRip() throws IOException {
        TapasticRipper ripper = new TapasticRipper(new URL("http://tapastic.com/episode/2139"));
        testRipper(ripper);
    }
}
