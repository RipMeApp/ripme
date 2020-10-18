package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.TeenplanetRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TeenplanetRipperTest extends RippersTest {
    @Test
    public void testTeenplanetRip() throws IOException {
        TeenplanetRipper ripper = new TeenplanetRipper(new URL("http://teenplanet.org/galleries/the-perfect-side-of-me-6588.html"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("http://teenplanet.org/galleries/the-perfect-side-of-me-6588.html");
        TeenplanetRipper ripper = new TeenplanetRipper(url);
        Assertions.assertEquals("the-perfect-side-of-me-6588", ripper.getGID(url));
    }
}
