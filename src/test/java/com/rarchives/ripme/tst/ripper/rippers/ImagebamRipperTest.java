package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ImagebamRipper;

public class ImagebamRipperTest extends RippersTest {
    public void testImagebamRip() throws IOException {
        ImagebamRipper ripper = new ImagebamRipper(new URL("http://www.imagebam.com/gallery/488cc796sllyf7o5srds8kpaz1t4m78i"));
        testRipper(ripper);
    }
}
