package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.video.GfycatRipper;
import java.io.IOException;
import java.net.URL;


public class GfycatRipperTest extends RippersTest {
    
    /**
     * REDDIT TEST
     * Tests a good GfycatURL (no "/gifs/detail")
     * @throws IOException 
     */
    public void testRedditGfyGoodURL() throws IOException {
        GfycatRipper ripper = new GfycatRipper(new URL("https://www.reddit.com/r/bottesting/comments/7msozf/good_link/"));
        testRipper(ripper);
    }
    
    
    /**
     * REDDIT TEST
     * Tests a Bad URL with the "/gifs/detail" inside.
     * @throws IOException 
     */
    public void testRedditGfyBadURL() throws IOException {
        GfycatRipper ripper = new GfycatRipper(new URL("https://www.reddit.com/r/bottesting/comments/7msmhi/bad_link/"));
        testRipper(ripper);
    }
    
    
}
