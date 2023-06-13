package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.GirlsOfDesireRipper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class GirlsOfDesireRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testGirlsofdesireAlbum() throws IOException, URISyntaxException {
        GirlsOfDesireRipper ripper = new GirlsOfDesireRipper(new URI("http://www.girlsofdesire.org/galleries/krillia/").toURL());
        testRipper(ripper);
    }
}
