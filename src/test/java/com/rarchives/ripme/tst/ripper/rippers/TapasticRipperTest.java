package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.TapasticRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TapasticRipperTest extends RippersTest {
    @Test
    @Disabled("ripper broken")
    public void testTapasticRip() throws IOException, URISyntaxException {
        TapasticRipper ripper = new TapasticRipper(new URI("https://tapas.io/series/TPIAG").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://tapas.io/series/TPIAG").toURL();
        TapasticRipper ripper = new TapasticRipper(url);
        Assertions.assertEquals("series_ TPIAG", ripper.getGID(url));
    }
}
