package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.OglafRipper;

import org.junit.jupiter.api.Test;

public class OglafRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException, URISyntaxException {
        OglafRipper ripper = new OglafRipper(new URI("http://oglaf.com/plumes/").toURL());
        testRipper(ripper);
    }
}