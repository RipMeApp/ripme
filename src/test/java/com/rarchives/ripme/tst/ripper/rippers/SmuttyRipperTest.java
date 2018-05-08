package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.SmuttyRipper;

public class SmuttyRipperTest extends RippersTest {
    public void testRip() throws IOException {
        SmuttyRipper ripper = new SmuttyRipper(new URL("https://smutty.com/user/QUIGON/"));
        testRipper(ripper);
    }
}
