package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.JagodibujaRipper;
<<<<<<< HEAD
=======
import org.junit.jupiter.api.Disabled;
>>>>>>> upstream/master
import org.junit.jupiter.api.Test;

public class JagodibujaRipperTest extends RippersTest {
    @Test
<<<<<<< HEAD
=======
    @Disabled("fails on github ubuntu automated PR check 2020-07-29")
>>>>>>> upstream/master
    public void testJagodibujaRipper() throws IOException {
        // a photo set
        JagodibujaRipper ripper = new JagodibujaRipper(new URL("http://www.jagodibuja.com/comic-in-me/"));
        testRipper(ripper);
    }
}