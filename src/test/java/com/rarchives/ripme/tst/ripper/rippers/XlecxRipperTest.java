package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.XlecxRipper;

public class XlecxRipperTest extends RippersTest {
    public void testAlbum() throws IOException {
        XlecxRipper ripper = new XlecxRipper(new URL("http://xlecx.com/4937-tokimeki-nioi.html"));
        testRipper(ripper);
    }
}
