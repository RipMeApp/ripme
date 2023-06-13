package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.ModelmayhemRipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ModelmayhemRipperTest extends RippersTest {

    @Test
    @Disabled("Broken ripper")
    public void testModelmayhemRip() throws IOException, URISyntaxException {
        ModelmayhemRipper ripper = new ModelmayhemRipper(
                new URI("https://www.modelmayhem.com/portfolio/520206/viewall").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        ModelmayhemRipper ripper = new ModelmayhemRipper(
                new URI("https://www.modelmayhem.com/portfolio/520206/viewall").toURL());
        Assertions.assertEquals("520206", ripper.getGID(new URI("https://www.modelmayhem.com/portfolio/520206/viewall").toURL()));
    }
}
