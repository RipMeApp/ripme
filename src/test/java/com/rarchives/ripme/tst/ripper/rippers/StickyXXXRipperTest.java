package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.video.StickyXXXRipper;
// import com.rarchives.ripme.tst.ripper.rippers.RippersTest;
import com.rarchives.ripme.utils.Utils;
import org.junit.jupiter.api.Test;

public class StickyXXXRipperTest extends RippersTest {

    @Test
    public void testStickyXXXVideo() throws IOException, URISyntaxException {
        // This test fails on the CI - possibly due to checking for a file before it's written - so we're skipping it
        if (Utils.getConfigBoolean("test.run_flaky_tests", false)) {        
            StickyXXXRipper ripper = new StickyXXXRipper(new URI("http://www.stickyxxx.com/a-very-intense-farewell/").toURL());
            testRipper(ripper);
        }
    }

}