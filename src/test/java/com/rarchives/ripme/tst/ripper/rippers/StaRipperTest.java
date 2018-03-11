package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.StaRipper;

public class StaRipperTest extends RippersTest {
    public void testRip() throws IOException {
        StaRipper ripper = new StaRipper(new URL("https://sta.sh/2hn9rtavr1g"));
        testRipper(ripper);
    }
}