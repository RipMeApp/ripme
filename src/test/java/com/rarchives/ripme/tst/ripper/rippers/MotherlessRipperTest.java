package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.rippers.MotherlessRipper;

public class MotherlessRipperTest extends RippersTest {
    @Test
    public void testMotherlessAlbumRip() throws IOException, URISyntaxException {
        MotherlessRipper ripper = new MotherlessRipper(new URI("https://motherless.com/GI471FFFF").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testMotherlessVideoRip() throws IOException, URISyntaxException {
        MotherlessRipper ripper = new MotherlessRipper(new URI("https://motherless.com/0D2D897").toURL());
        testRipper(ripper);
    }
}
