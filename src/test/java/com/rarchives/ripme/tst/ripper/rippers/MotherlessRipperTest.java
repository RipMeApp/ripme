package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.MotherlessRipper;

public class MotherlessRipperTest extends RippersTest {
    // https://github.com/RipMeApp/ripme/issues/238 - MotherlessRipperTest is flaky on Travis CI
    public void testMotherlessAlbumRip() throws IOException {
        MotherlessRipper ripper = new MotherlessRipper(new URL("http://motherless.com/G4DAA18D"));
        testRipper(ripper);
    }
}
