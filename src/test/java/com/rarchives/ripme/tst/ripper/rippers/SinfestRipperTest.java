package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.SinfestRipper;
<<<<<<< HEAD

public class SinfestRipperTest extends RippersTest {
=======
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SinfestRipperTest extends RippersTest {
    @Test
>>>>>>> upstream/master
    public void testRip() throws IOException {
        SinfestRipper ripper = new SinfestRipper(new URL("http://sinfest.net/view.php?date=2000-01-17"));
        testRipper(ripper);
    }

<<<<<<< HEAD
    public void testGetGID() throws IOException {
        URL url = new URL("http://sinfest.net/view.php?date=2000-01-17");
        SinfestRipper ripper = new SinfestRipper(url);
        assertEquals("2000-01-17", ripper.getGID(url));
=======
    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("http://sinfest.net/view.php?date=2000-01-17");
        SinfestRipper ripper = new SinfestRipper(url);
        Assertions.assertEquals("2000-01-17", ripper.getGID(url));
>>>>>>> upstream/master
    }
}