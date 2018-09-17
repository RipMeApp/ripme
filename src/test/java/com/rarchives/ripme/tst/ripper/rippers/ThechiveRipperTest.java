package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ThechiveRipper;

public class ThechiveRipperTest extends RippersTest {
    public void testPahealRipper() throws IOException {
        ThechiveRipper ripper = new ThechiveRipper(new URL("https://thechive.com/2018/09/17/daily-morning-awesomeness-35-photos-555/"));
        testRipper(ripper);
    }
}