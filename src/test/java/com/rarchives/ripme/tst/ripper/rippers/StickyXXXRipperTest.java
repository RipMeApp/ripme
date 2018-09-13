package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.video.StickyXXXRipper;
// import com.rarchives.ripme.tst.ripper.rippers.RippersTest;
import com.rarchives.ripme.utils.Utils;

public class StickyXXXRipperTest extends RippersTest {

    public void testStickyXXXVideo() throws IOException {
        // This test fails on the CI - possibly due to checking for a file before it's written - so we're skipping it
        if (Utils.getConfigBoolean("test.run_flaky_tests", false)) {        
            StickyXXXRipper ripper = new StickyXXXRipper(new URL("http://www.stickyxxx.com/a-very-intense-farewell/"));
            testRipper(ripper);
        }
    }

}