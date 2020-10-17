package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.RulePornRipper;
<<<<<<< HEAD

public class RulePornRipperTest extends RippersTest {
    public void testRip() throws IOException {
        RulePornRipper ripper = new RulePornRipper(new URL("https://ruleporn.com/are-you-going-to-fill-my-lil-pussy-up/"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://ruleporn.com/are-you-going-to-fill-my-lil-pussy-up/");
        RulePornRipper ripper = new RulePornRipper(url);
        assertEquals("are-you-going-to-fill-my-lil-pussy-up", ripper.getGID(url));
=======
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RulePornRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException {
        RulePornRipper ripper = new RulePornRipper(new URL("https://ruleporn.com/tosh/"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://ruleporn.com/tosh/");
        RulePornRipper ripper = new RulePornRipper(url);
        Assertions.assertEquals("tosh", ripper.getGID(url));
>>>>>>> upstream/master
    }
}