package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.BlackbrickroadofozRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class BlackbrickroadofozRipperTest extends RippersTest {
    @Test
    @Disabled("Commented out on 02/04/2019 because the serve has been down for a while")
    public void testRip() throws IOException, URISyntaxException {
        BlackbrickroadofozRipper ripper = new BlackbrickroadofozRipper(
                new URI("http://www.blackbrickroadofoz.com/comic/beginning").toURL());
        testRipper(ripper);
    }
}
