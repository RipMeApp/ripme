package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.MotherlessRipper;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class MotherlessRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testMotherlessAlbumRip() throws IOException, URISyntaxException {
        MotherlessRipper ripper = new MotherlessRipper(new URI("https://motherless.com/G1168D90").toURL());
        testRipper(ripper);
    }
}
