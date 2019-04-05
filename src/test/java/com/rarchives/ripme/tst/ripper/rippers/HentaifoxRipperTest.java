package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.HentaifoxRipper;

public class HentaifoxRipperTest extends RippersTest {
    public void testRip() throws IOException {
        HentaifoxRipper ripper = new HentaifoxRipper(new URL("https://hentaifox.com/gallery/38544/"));
        testRipper(ripper);
    }
}
