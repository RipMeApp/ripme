package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.SinfestRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class SinfestRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testRip() throws IOException, URISyntaxException {
        SinfestRipper ripper = new SinfestRipper(new URI("http://sinfest.net/view.php?date=2000-01-17").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("http://sinfest.net/view.php?date=2000-01-17").toURL();
        SinfestRipper ripper = new SinfestRipper(url);
        Assertions.assertEquals("2000-01-17", ripper.getGID(url));
    }
}