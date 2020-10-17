package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.SmuttyRipper;
<<<<<<< HEAD

public class SmuttyRipperTest extends RippersTest {
=======
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SmuttyRipperTest extends RippersTest {
    @Test
>>>>>>> upstream/master
    public void testRip() throws IOException {
        SmuttyRipper ripper = new SmuttyRipper(new URL("https://smutty.com/user/QUIGON/"));
        testRipper(ripper);
    }

<<<<<<< HEAD
    public void testGetGID() throws IOException {
        URL url = new URL("https://smutty.com/user/QUIGON/");
        SmuttyRipper ripper = new SmuttyRipper(url);
        assertEquals("QUIGON", ripper.getGID(url));
=======
    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://smutty.com/user/QUIGON/");
        SmuttyRipper ripper = new SmuttyRipper(url);
        Assertions.assertEquals("QUIGON", ripper.getGID(url));
>>>>>>> upstream/master
    }
}
