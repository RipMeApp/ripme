package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ArtAlleyRipper;
import org.junit.jupiter.api.Test;

public class ArtAlleyRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException {
        ArtAlleyRipper ripper = new ArtAlleyRipper(new URL("https://artalley.social/@curator/media"));
        testRipper(ripper);
    }
}
