package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.TapasticRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TapasticRipperTest extends RippersTest {
    @Test
    @Disabled("ripper broken")
    public void testTapasticRip() throws IOException {
        TapasticRipper ripper = new TapasticRipper(new URL("https://tapas.io/series/TPIAG"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://tapas.io/series/TPIAG");
        TapasticRipper ripper = new TapasticRipper(url);
        Assertions.assertEquals("series_ TPIAG", ripper.getGID(url));
    }
}
