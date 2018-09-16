package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.Tubex6Ripper;

public class Tubex6RipperTest extends RippersTest {
    public void testRip() throws IOException {
        Tubex6Ripper ripper = new Tubex6Ripper(new URL("http://www.tubex6.com/my-sister-sleeps-naked-1/"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("http://www.tubex6.com/my-sister-sleeps-naked-1/");
        Tubex6Ripper ripper = new Tubex6Ripper(url);
        assertEquals("my-sister-sleeps-naked-1", ripper.getGID(url));
    }
}