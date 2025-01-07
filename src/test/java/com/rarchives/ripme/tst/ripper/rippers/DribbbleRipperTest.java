package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.DribbbleRipper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DribbbleRipperTest extends RippersTest {
    @Test
    @Disabled("test or ripper broken")
    public void testDribbbleRip() throws IOException, URISyntaxException {
        DribbbleRipper ripper = new DribbbleRipper(new URI("https://dribbble.com/typogriff").toURL());
        testRipper(ripper);
    }
}
