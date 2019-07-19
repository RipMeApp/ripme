package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.BlackbrickroadofozRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

public class BlackbrickroadofozRipperTest extends RippersTest {
    @Test
    @Disabled("Commented out on 02/04/2019 because the serve has been down for a while")
    public void testRip() throws IOException {
        BlackbrickroadofozRipper ripper = new BlackbrickroadofozRipper(
                new URL("http://www.blackbrickroadofoz.com/comic/beginning"));
        testRipper(ripper);
    }
}
