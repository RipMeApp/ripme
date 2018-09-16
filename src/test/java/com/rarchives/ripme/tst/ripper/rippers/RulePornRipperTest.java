package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.RulePornRipper;

public class RulePornRipperTest extends RippersTest {
    public void testRip() throws IOException {
        RulePornRipper ripper = new RulePornRipper(new URL("https://ruleporn.com/are-you-going-to-fill-my-lil-pussy-up/"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://ruleporn.com/are-you-going-to-fill-my-lil-pussy-up/");
        RulePornRipper ripper = new RulePornRipper(url);
        assertEquals("are-you-going-to-fill-my-lil-pussy-up", ripper.getGID(url));
    }
}