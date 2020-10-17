package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.TapasticRipper;
<<<<<<< HEAD
=======
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
>>>>>>> upstream/master
import org.junit.jupiter.api.Test;

public class TapasticRipperTest extends RippersTest {
    @Test
<<<<<<< HEAD
    public void testTapasticRip() throws IOException {
        TapasticRipper ripper = new TapasticRipper(new URL("https://tapas.io/series/tsiwbakd-comic"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://tapas.io/series/tsiwbakd-comic");
        TapasticRipper ripper = new TapasticRipper(url);
        assertEquals("series_ tsiwbakd-comic", ripper.getGID(url));
=======
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
>>>>>>> upstream/master
    }
}
