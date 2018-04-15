package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.BlackbrickroadofozRipper;

import java.io.IOException;
import java.net.URL;

public class BlackbrickroadofozRipperTest extends RippersTest {
    public void testRip() throws IOException {
        BlackbrickroadofozRipper ripper = new BlackbrickroadofozRipper(new URL("http://www.blackbrickroadofoz.com/comic/beginning"));
        testRipper(ripper);
    }
}
