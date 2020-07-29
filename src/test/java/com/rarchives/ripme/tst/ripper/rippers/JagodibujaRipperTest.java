package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.JagodibujaRipper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class JagodibujaRipperTest extends RippersTest {
    @Test
    @Disabled("fails on github ubuntu automated PR check 2020-07-29")
    public void testJagodibujaRipper() throws IOException {
        // a photo set
        JagodibujaRipper ripper = new JagodibujaRipper(new URL("http://www.jagodibuja.com/comic-in-me/"));
        testRipper(ripper);
    }
}