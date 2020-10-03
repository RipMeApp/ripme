package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.DynastyscansRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DynastyscansRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException {
        DynastyscansRipper ripper = new DynastyscansRipper(new URL("https://dynasty-scans.com/chapters/under_one_roof_ch01"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        DynastyscansRipper ripper = new DynastyscansRipper(new URL("https://dynasty-scans.com/chapters/under_one_roof_ch01"));
        Assertions.assertEquals("under_one_roof_ch01", ripper.getGID(new URL("https://dynasty-scans.com/chapters/under_one_roof_ch01")));
    }
}
