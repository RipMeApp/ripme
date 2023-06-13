package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.RulePornRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RulePornRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException, URISyntaxException {
        RulePornRipper ripper = new RulePornRipper(new URI("https://ruleporn.com/tosh/").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://ruleporn.com/tosh/").toURL();
        RulePornRipper ripper = new RulePornRipper(url);
        Assertions.assertEquals("tosh", ripper.getGID(url));
    }
}