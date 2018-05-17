package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.BatoRipper;

public class BatoRipperTest extends RippersTest {
    public void testRip() throws IOException {
        BatoRipper ripper = new BatoRipper(new URL("https://bato.to/chapter/1207152"));
        testRipper(ripper);
    }
}
