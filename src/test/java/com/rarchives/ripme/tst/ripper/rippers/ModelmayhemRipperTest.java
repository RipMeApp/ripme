package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.rippers.ModelmayhemRipper;

public class ModelmayhemRipperTest extends RippersTest {
    @Test
    public void testModelmayhemRip() throws IOException, URISyntaxException {
        ModelmayhemRipper ripper = new ModelmayhemRipper(
                new URI("https://www.modelmayhem.com/portfolio/4829413/viewall").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        ModelmayhemRipper ripper = new ModelmayhemRipper(
                new URI("https://www.modelmayhem.com/portfolio/4829413/viewall").toURL());
        Assertions.assertEquals("4829413",
                ripper.getGID(new URI("https://www.modelmayhem.com/portfolio/4829413/viewall").toURL()));
    }
}
