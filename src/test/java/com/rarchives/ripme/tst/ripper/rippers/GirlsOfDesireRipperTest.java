package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.GirlsOfDesireRipper;

public class GirlsOfDesireRipperTest extends RippersTest {
    public void testGirlsofdesireAlbum() throws IOException {
        GirlsOfDesireRipper ripper = new GirlsOfDesireRipper(new URL("http://www.girlsofdesire.org/galleries/krillia/"));
        testRipper(ripper);
    }
}
