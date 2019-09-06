package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.GfycatRipper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;


public class GfycatRipperTest extends RippersTest {
    
    /**
     * Rips correctly formatted URL directly from Gfycat
     * @throws IOException 
     */
    @Test
    public void testGfycatGoodURL() throws IOException{
        GfycatRipper ripper = new GfycatRipper(new URL("https://gfycat.com/TemptingExcellentIchthyosaurs"));
        testRipper(ripper);
    }
    /**
     * Rips badly formatted URL directly from Gfycat
     * @throws IOException 
     */
    public void testGfycatBadURL() throws IOException {
        GfycatRipper ripper  = new GfycatRipper(new URL("https://gfycat.com/gifs/detail/limitedtestyamericancrow"));
        testRipper(ripper);
    }

    /**
     * Rips a Gfycat profile
     * @throws IOException 
     */
    public void testGfycatProfile() throws IOException {
        GfycatRipper ripper  = new GfycatRipper(new URL("https://gfycat.com/@golbanstorage"));
        testRipper(ripper);
    }
    
    /**
     * Rips a Gfycat amp link 
     * @throws IOException 
     */
    public void testGfycatAmp() throws IOException {
        GfycatRipper ripper = new GfycatRipper(new URL("https://gfycat.com/amp/TemptingExcellentIchthyosaurs"));
        testRipper(ripper);
    }
}
