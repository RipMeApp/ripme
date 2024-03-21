package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ImagevenueRipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ImagevenueRipperTest extends RippersTest {
    @Test
    @Disabled("See https://github.com/RipMeApp/ripme/issues/1202")
    public void testImagevenueRip() throws IOException, URISyntaxException {
        ImagevenueRipper ripper = new ImagevenueRipper(
                new URI("http://img120.imagevenue.com/galshow.php?gal=gallery_1373818527696_191lo").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("http://img120.imagevenue.com/galshow.php?gal=gallery_1373818527696_191lo").toURL();
        ImagevenueRipper ripper = new ImagevenueRipper(url);
        Assertions.assertEquals("gallery_1373818527696_191lo", ripper.getGID(url));
    }
}
