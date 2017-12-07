package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ZizkiRipper;

public class ZizkiRipperTest extends RippersTest {
    public void testRip() throws IOException {
        ZizkiRipper ripper = new ZizkiRipper(new URL("http://zizki.com/dee-chorde/we-got-spirit"));
        testRipper(ripper);
    }
}
