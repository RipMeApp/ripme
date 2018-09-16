package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.Tubex6Ripper;

public class RulePornRipperTest extends RippersTest {
    public void testRip() throws IOException {
        Tubex6Ripper ripper = new Tubex6Ripper(new URL("https://ruleporn.com/are-you-going-to-fill-my-lil-pussy-up/"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://ruleporn.com/are-you-going-to-fill-my-lil-pussy-up/");
        Tubex6Ripper ripper = new Tubex6Ripper(url);
        assertEquals("are-you-going-to-fill-my-lil-pussy-up", ripper.getGID(url));
    }
}