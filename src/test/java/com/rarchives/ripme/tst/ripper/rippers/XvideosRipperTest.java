package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.XvideosRipper;
import org.junit.jupiter.api.Test;

public class XvideosRipperTest extends RippersTest {
    @Test
    public void testXhamsterAlbum1() throws IOException, URISyntaxException {
        XvideosRipper ripper = new XvideosRipper(new URI("https://www.xvideos.com/video23515878/dee_s_pool_toys").toURL());
        testRipper(ripper);
    }

}
