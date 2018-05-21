package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.SinfestRipper;

public class SinfestRipperTest extends RippersTest {
    public void testRip() throws IOException {
        SinfestRipper ripper = new SinfestRipper(new URL("http://sinfest.net/view.php?date=2000-01-17"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("http://sinfest.net/view.php?date=2000-01-17");
        SinfestRipper ripper = new SinfestRipper(url);
        assertEquals("2000-01-17", ripper.getGID(url));
    }
}