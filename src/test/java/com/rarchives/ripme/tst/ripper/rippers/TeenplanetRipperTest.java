package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.TeenplanetRipper;

public class TeenplanetRipperTest extends RippersTest {
    public void testTeenplanetRip() throws IOException {
        TeenplanetRipper ripper = new TeenplanetRipper(new URL("http://teenplanet.org/galleries/the-perfect-side-of-me-6588.html"));
        testRipper(ripper);
    }
}
