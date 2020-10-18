package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.StaRipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class StaRipperTest extends RippersTest {
    @Test
    @Disabled("Ripper broken, Nullpointer exception")
    public void testRip() throws IOException {
        StaRipper ripper = new StaRipper(new URL("https://sta.sh/01umpyuxi4js"));
        testRipper(ripper);
    }

    @Test
    @Disabled
    public void testGetGID() throws IOException {
        URL url = new URL("https://sta.sh/01umpyuxi4js");
        StaRipper ripper = new StaRipper(url);
        Assertions.assertEquals("01umpyuxi4js", ripper.getGID(url));
    }
}