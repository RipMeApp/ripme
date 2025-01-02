package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.AerisdiesRipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class AerisdiesRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testAlbum() throws IOException, URISyntaxException {
        AerisdiesRipper ripper = new AerisdiesRipper(new URI("http://www.aerisdies.com/html/lb/alb_1097_1.html").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testSubAlbum() throws IOException, URISyntaxException {
        AerisdiesRipper ripper = new AerisdiesRipper(new URI("http://www.aerisdies.com/html/lb/alb_3692_1.html").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testDjAlbum() throws IOException, URISyntaxException {
        AerisdiesRipper ripper = new AerisdiesRipper(new URI("http://www.aerisdies.com/html/lb/douj_5230_1.html").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("http://www.aerisdies.com/html/lb/douj_5230_1.html").toURL();
        AerisdiesRipper ripper = new AerisdiesRipper(url);
        Assertions.assertEquals("5230", ripper.getGID(url));
    }

    // TODO: Add a test for an album with a title.
}
