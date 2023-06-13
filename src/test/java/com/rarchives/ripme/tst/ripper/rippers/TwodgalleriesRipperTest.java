package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.TwodgalleriesRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TwodgalleriesRipperTest extends RippersTest {
    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/182")
    public void testTwodgalleriesRip() throws IOException, URISyntaxException {
        TwodgalleriesRipper ripper = new TwodgalleriesRipper(
                new URI("http://www.2dgalleries.com/artist/regis-loisel-6477").toURL());
        testRipper(ripper);
    }

}
