package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.DribbbleRipper;

public class DribbbleRipperTest extends RippersTest {
    public void testDribbbleRip() throws IOException {
        DribbbleRipper ripper = new DribbbleRipper(new URL("https://dribbble.com/typogriff"));
        testRipper(ripper);
    }
}
