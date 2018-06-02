package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.AerisdiesRipper;

public class AerisdiesRipperTest extends RippersTest {
    public void testAlbum() throws IOException {
        AerisdiesRipper ripper = new AerisdiesRipper(new URL("http://www.aerisdies.com/html/lb/alb_1097_1.html"));
        testRipper(ripper);
    }

    public void testSubAlbum() throws IOException {
        AerisdiesRipper ripper = new AerisdiesRipper(new URL("http://www.aerisdies.com/html/lb/alb_3692_1.html"));
        testRipper(ripper);
    }

    public void testDjAlbum() throws IOException {
        AerisdiesRipper ripper = new AerisdiesRipper(new URL("http://www.aerisdies.com/html/lb/douj_5230_1.html"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("http://www.aerisdies.com/html/lb/douj_5230_1.html");
        AerisdiesRipper ripper = new AerisdiesRipper(url);
        assertEquals("5230", ripper.getGID(url));
    }

    // TODO: Add a test for an album with a title.
}
