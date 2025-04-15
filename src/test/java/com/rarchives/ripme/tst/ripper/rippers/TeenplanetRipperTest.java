package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.TeenplanetRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class TeenplanetRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testTeenplanetRip() throws IOException, URISyntaxException {
        TeenplanetRipper ripper = new TeenplanetRipper(new URI("http://teenplanet.org/galleries/the-perfect-side-of-me-6588.html").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("http://teenplanet.org/galleries/the-perfect-side-of-me-6588.html").toURL();
        TeenplanetRipper ripper = new TeenplanetRipper(url);
        Assertions.assertEquals("the-perfect-side-of-me-6588", ripper.getGID(url));
    }
}
