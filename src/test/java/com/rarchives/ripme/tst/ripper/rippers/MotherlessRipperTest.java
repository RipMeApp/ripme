package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.MotherlessRipper;

import org.junit.jupiter.api.Test;

public class MotherlessRipperTest extends RippersTest {
    @Test
    public void testMotherlessAlbumRip() throws IOException {
        MotherlessRipper ripper = new MotherlessRipper(new URL("https://motherless.com/G1168D90"));
        testRipper(ripper);
    }
}
