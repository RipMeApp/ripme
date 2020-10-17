package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.StaRipper;

<<<<<<< HEAD
=======
import org.junit.jupiter.api.Assertions;
>>>>>>> upstream/master
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class StaRipperTest extends RippersTest {
    @Test
<<<<<<< HEAD
    @Disabled("404 Link")
    public void testRip() throws IOException {
        StaRipper ripper = new StaRipper(new URL("https://sta.sh/2hn9rtavr1g"));
=======
    @Disabled("Ripper broken, Nullpointer exception")
    public void testRip() throws IOException {
        StaRipper ripper = new StaRipper(new URL("https://sta.sh/01umpyuxi4js"));
>>>>>>> upstream/master
        testRipper(ripper);
    }

    @Test
<<<<<<< HEAD
    @Disabled("404 Link")
    public void testGetGID() throws IOException {
        URL url = new URL("https://sta.sh/2hn9rtavr1g");
        StaRipper ripper = new StaRipper(url);
        assertEquals("2hn9rtavr1g", ripper.getGID(url));
=======
    @Disabled
    public void testGetGID() throws IOException {
        URL url = new URL("https://sta.sh/01umpyuxi4js");
        StaRipper ripper = new StaRipper(url);
        Assertions.assertEquals("01umpyuxi4js", ripper.getGID(url));
>>>>>>> upstream/master
    }
}