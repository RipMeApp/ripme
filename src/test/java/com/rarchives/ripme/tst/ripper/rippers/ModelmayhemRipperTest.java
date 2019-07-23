package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ModelmayhemRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ModelmayhemRipperTest extends RippersTest {

    @Test
    @Disabled("Broken ripper")
    public void testModelmayhemRip() throws IOException {
        ModelmayhemRipper ripper = new ModelmayhemRipper(
                new URL("https://www.modelmayhem.com/portfolio/520206/viewall"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        ModelmayhemRipper ripper = new ModelmayhemRipper(
                new URL("https://www.modelmayhem.com/portfolio/520206/viewall"));
        assertEquals("520206", ripper.getGID(new URL("https://www.modelmayhem.com/portfolio/520206/viewall")));
    }
}
