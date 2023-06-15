package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.GfycatRipper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class GfycatRipperTest extends RippersTest {
    
    /**
     * Rips correctly formatted URL directly from Gfycat
     */
    @Test
    public void testGfycatGoodURL() throws IOException, URISyntaxException {
        GfycatRipper ripper = new GfycatRipper(new URI("https://gfycat.com/TemptingExcellentIchthyosaurs").toURL());
        testRipper(ripper);
    }
    /**
     * Rips badly formatted URL directly from Gfycat
     */
    public void testGfycatBadURL() throws IOException, URISyntaxException {
        GfycatRipper ripper  = new GfycatRipper(new URI("https://gfycat.com/gifs/detail/limitedtestyamericancrow").toURL());
        testRipper(ripper);
    }

    /**
     * Rips a Gfycat profile
     */
    public void testGfycatProfile() throws IOException, URISyntaxException {
        GfycatRipper ripper  = new GfycatRipper(new URI("https://gfycat.com/@golbanstorage").toURL());
        testRipper(ripper);
    }
    
    /**
     * Rips a Gfycat amp link 
     * @throws IOException 
     */
    public void testGfycatAmp() throws IOException, URISyntaxException {
        GfycatRipper ripper = new GfycatRipper(new URI("https://gfycat.com/amp/TemptingExcellentIchthyosaurs").toURL());
        testRipper(ripper);
    }

    /**
     * Rips a Gfycat profile with special characters in username
     */
    public void testGfycatSpecialChar() throws IOException, URISyntaxException {
        GfycatRipper ripper = new GfycatRipper(new URI("https://gfycat.com/@rsss.kr").toURL());
        testRipper(ripper);
    }
}
