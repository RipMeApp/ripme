package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ImagearnRipper;

public class ImagearnRipperTest extends RippersTest {
    public void testImagearnRip() throws IOException {
        ImagearnRipper ripper = new ImagearnRipper(new URL("http://imagearn.com//gallery.php?id=578682"));
        testRipper(ripper);
    }
}
