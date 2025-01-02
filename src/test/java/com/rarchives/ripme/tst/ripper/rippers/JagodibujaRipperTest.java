package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.JagodibujaRipper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class JagodibujaRipperTest extends RippersTest {
    @Test
    @Disabled("fails on github ubuntu automated PR check 2020-07-29")
    public void testJagodibujaRipper() throws IOException, URISyntaxException {
        // a photo set
        JagodibujaRipper ripper = new JagodibujaRipper(new URI("http://www.jagodibuja.com/comic-in-me/").toURL());
        testRipper(ripper);
    }
}