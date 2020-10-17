package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.VscoRipper;

<<<<<<< HEAD
=======
import org.junit.jupiter.api.Assertions;
>>>>>>> upstream/master
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

public class VscoRipperTest extends RippersTest {

    /**
     * Testing single image.
     * 
     * @throws IOException
     */
    @Test
    public void testSingleImageRip() throws IOException {
        VscoRipper ripper = new VscoRipper(new URL("https://vsco.co/jonathangodoy/media/5d1aec76bb669a128035e98a"));
        testRipper(ripper);
    }

    /**
<<<<<<< HEAD
     * Tests profile rip.
     * 
     * @throws IOException
     */
    @Test
    public void testProfileRip() throws IOException {
        VscoRipper ripper = new VscoRipper(new URL("https://vsco.co/jonathangodoy/images/1"));
        testRipper(ripper);
    }

    /**
     * Prevents Bug #679 from happening again.
=======
     * Tests profile rip., Prevents Bug #679 from happening again.
>>>>>>> upstream/master
     * https://github.com/RipMeApp/ripme/issues/679
     * 
     * @throws IOException
     */
    @Test
    public void testHyphenatedRip() throws IOException {
<<<<<<< HEAD
        VscoRipper ripper = new VscoRipper(new URL("https://vsco.co/jolly-roger/images/1"));
=======
        VscoRipper ripper = new VscoRipper(new URL("https://vsco.co/jolly-roger/gallery"));
>>>>>>> upstream/master
        testRipper(ripper);
    }

    /**
     * Make sure it names the folder something sensible.
     * 
     * @throws IOException
     */
    @Test
    public void testGetGID() throws IOException {
<<<<<<< HEAD
        URL url = new URL("https://vsco.co/minijello/media/571cd612542220261a123441");

        VscoRipper ripper = new VscoRipper(url);

        assertEquals("Failed to get GID", "minijello/571cd", ripper.getGID(url));
=======
        URL url = new URL("https://vsco.co/jolly-roger/media/590359c4ade3041f2658f407");

        VscoRipper ripper = new VscoRipper(url);

        Assertions.assertEquals("jolly-roger/59035", ripper.getGID(url), "Failed to get GID");
>>>>>>> upstream/master
    }

}
