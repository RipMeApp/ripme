package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.VscoRipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class VscoRipperTest extends RippersTest {

    /**
     * Testing single image.
     * 
     * @throws IOException
     */
    @Test
    public void testSingleImageRip() throws IOException, URISyntaxException {
        VscoRipper ripper = new VscoRipper(new URI("https://vsco.co/jolly-roger/media/597ce449846079297b3f7cf3").toURL());
        testRipper(ripper);
    }

    /**
     * Tests profile rip., Prevents Bug #679 from happening again.
     * https://github.com/RipMeApp/ripme/issues/679
     * 
     * @throws IOException
     */
    @Test
    public void testHyphenatedRip() throws IOException, URISyntaxException {
        VscoRipper ripper = new VscoRipper(new URI("https://vsco.co/jolly-roger/gallery").toURL());
        testRipper(ripper);
    }

    /**
     * Make sure it names the folder something sensible.
     * 
     * @throws IOException
     */
    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://vsco.co/jolly-roger/media/590359c4ade3041f2658f407").toURL();

        VscoRipper ripper = new VscoRipper(url);

        Assertions.assertEquals("jolly-roger/59035", ripper.getGID(url), "Failed to get GID");
    }

}
