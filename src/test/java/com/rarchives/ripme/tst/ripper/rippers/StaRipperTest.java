package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.StaRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class StaRipperTest extends RippersTest {
    @Test
    @Disabled("404 Link")
    public void testRip() throws IOException {
        StaRipper ripper = new StaRipper(new URL("https://sta.sh/2hn9rtavr1g"));
        testRipper(ripper);
    }

    @Test
    @Disabled("404 Link")
    public void testGetGID() throws IOException {
        URL url = new URL("https://sta.sh/2hn9rtavr1g");
        StaRipper ripper = new StaRipper(url);
        assertEquals("2hn9rtavr1g", ripper.getGID(url));
    }
}