package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.video.StickyXXXRipper;
import com.rarchives.ripme.tst.ripper.rippers.RippersTest;

public class StickyXXXRipperTest extends RippersTest {

    public void testStickyXXXVideo() throws IOException {
        StickyXXXRipper ripper = new StickyXXXRipper(new URL("http://www.stickyxxx.com/a-very-intense-farewell/"));
        testRipper(ripper);
    }

}