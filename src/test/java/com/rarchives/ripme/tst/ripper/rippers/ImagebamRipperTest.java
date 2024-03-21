package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.ImagebamRipper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class ImagebamRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testImagebamRip() throws IOException, URISyntaxException {
        ImagebamRipper ripper = new ImagebamRipper(new URI("http://www.imagebam.com/gallery/488cc796sllyf7o5srds8kpaz1t4m78i").toURL());
        testRipper(ripper);
    }
}
