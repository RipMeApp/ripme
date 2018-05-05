package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.DynastyscansRipper;

public class DynastyscansRipperTest extends RippersTest {
    public void testRip() throws IOException {
        DynastyscansRipper ripper = new DynastyscansRipper(new URL("https://dynasty-scans.com/chapters/under_one_roof_ch01"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        DynastyscansRipper ripper = new DynastyscansRipper(new URL("https://dynasty-scans.com/chapters/under_one_roof_ch01"));
        assertEquals("under_one_roof_ch01", ripper.getGID(new URL("https://dynasty-scans.com/chapters/under_one_roof_ch01")));
    }
}
