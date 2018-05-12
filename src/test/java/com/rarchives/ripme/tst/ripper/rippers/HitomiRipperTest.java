package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.HitomiRipper;

public class HitomiRipperTest extends RippersTest {
    public void testRip() throws IOException {
        HitomiRipper ripper = new HitomiRipper(new URL("https://hitomi.la/galleries/975973.html"));
        testRipper(ripper);
        assertTrue(ripper.getGID(new URL("https://hitomi.la/galleries/975973.html")).equals("975973"));
    }
}
