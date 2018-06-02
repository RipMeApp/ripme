package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ImagevenueRipper;

public class ImagevenueRipperTest extends RippersTest {
    public void testImagevenueRip() throws IOException {
        ImagevenueRipper ripper = new ImagevenueRipper(new URL("http://img120.imagevenue.com/galshow.php?gal=gallery_1373818527696_191lo"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("http://img120.imagevenue.com/galshow.php?gal=gallery_1373818527696_191lo");
        ImagevenueRipper ripper = new ImagevenueRipper(url);
        assertEquals("gallery_1373818527696_191lo", ripper.getGID(url));
    }
}
