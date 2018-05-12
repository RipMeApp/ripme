package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ManganeloRipper;

public class ManganeloRipperTest extends RippersTest {
    public void testRip() throws IOException {
        ManganeloRipper ripper = new ManganeloRipper(new URL("http://manganelo.com/manga/black_clover"));
        testRipper(ripper);
    }
}
