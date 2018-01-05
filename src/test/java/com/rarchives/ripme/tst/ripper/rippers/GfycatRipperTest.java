package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.video.GfycatRipper;
import java.io.IOException;
import java.net.URL;


public class GfycatRipperTest extends RippersTest {
    
    /**
     * Rips correctly formatted URL directly from Gfycat
     * @throws IOException 
     */
    public void GfycatGoodURL() throws IOException{
        GfycatRipper ripper = new GfycatRipper(new URL("https://gfycat.com/TemptingExcellentIchthyosaurs"));
        testRipper(ripper);
    }
    /**
     * Rips badly formatted URL directly from Gfycat
     * @throws IOException 
     */
    public void GfycatBadURL() throws IOException {
        GfycatRipper ripper  = new GfycatRipper(new URL("https://gfycat.com/gifs/detail/limitedtestyamericancrow"));
        testRipper(ripper);
    }
}