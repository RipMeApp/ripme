package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.JagodibujaRipper;

public class JagodibujaRipperTest extends RippersTest {
    public void testJagodibujaRipper() throws IOException {
        // a photo set
        JagodibujaRipper ripper = new JagodibujaRipper(new URL("http://www.jagodibuja.com/comic-in-me/"));
        testRipper(ripper);
    }
}