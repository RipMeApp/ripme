package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.video.SpankbangRipper;
import com.rarchives.ripme.utils.Utils;

public class SpankBangRipperTest extends RippersTest {
    public void testSpankBangVideo() throws IOException {
        // This test fails on the CI so we skip it unless running locally
        if (Utils.getConfigBoolean("test.run_flaky_tests", false)) {
            SpankbangRipper ripper = new SpankbangRipper(new URL("https://spankbang.com/2a7fh/video/mdb901"));  //most popular video of all time on site; should stay up
            testRipper(ripper);
        }
    }

}
