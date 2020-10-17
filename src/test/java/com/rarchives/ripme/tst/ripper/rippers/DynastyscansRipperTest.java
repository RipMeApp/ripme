package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.DynastyscansRipper;
<<<<<<< HEAD

public class DynastyscansRipperTest extends RippersTest {
=======
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DynastyscansRipperTest extends RippersTest {
    @Test
>>>>>>> upstream/master
    public void testRip() throws IOException {
        DynastyscansRipper ripper = new DynastyscansRipper(new URL("https://dynasty-scans.com/chapters/under_one_roof_ch01"));
        testRipper(ripper);
    }

<<<<<<< HEAD
    public void testGetGID() throws IOException {
        DynastyscansRipper ripper = new DynastyscansRipper(new URL("https://dynasty-scans.com/chapters/under_one_roof_ch01"));
        assertEquals("under_one_roof_ch01", ripper.getGID(new URL("https://dynasty-scans.com/chapters/under_one_roof_ch01")));
=======
    @Test
    public void testGetGID() throws IOException {
        DynastyscansRipper ripper = new DynastyscansRipper(new URL("https://dynasty-scans.com/chapters/under_one_roof_ch01"));
        Assertions.assertEquals("under_one_roof_ch01", ripper.getGID(new URL("https://dynasty-scans.com/chapters/under_one_roof_ch01")));
>>>>>>> upstream/master
    }
}
