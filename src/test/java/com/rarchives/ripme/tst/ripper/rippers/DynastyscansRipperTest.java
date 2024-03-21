package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.DynastyscansRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class DynastyscansRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testRip() throws IOException, URISyntaxException {
        DynastyscansRipper ripper = new DynastyscansRipper(new URI("https://dynasty-scans.com/chapters/under_one_roof_ch01").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        DynastyscansRipper ripper = new DynastyscansRipper(new URI("https://dynasty-scans.com/chapters/under_one_roof_ch01").toURL());
        Assertions.assertEquals("under_one_roof_ch01", ripper.getGID(new URI("https://dynasty-scans.com/chapters/under_one_roof_ch01").toURL()));
    }
}
