package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.VscoRipper;
import java.io.IOException;
import java.net.URL;

public class VscoRipperTest extends RippersTest {
    
    /**
     * Testing single image.
     * @throws IOException 
     */
    public void testSingleImageRip() throws IOException {
        VscoRipper ripper = new VscoRipper(new URL("https://vsco.co/jonathangodoy/media/5b2c92b2e112534b6716cc7e"));
        testRipper(ripper);
    }
    
    /**
     * Tests profile rip.
     * @throws IOException 
     */
    public void testProfileRip() throws IOException {
        VscoRipper ripper = new VscoRipper(new URL("https://vsco.co/jonathangodoy/images/1"));
        testRipper(ripper);
    }
    
    /**
     * Prevents Bug #679 from happening again.
     * https://github.com/RipMeApp/ripme/issues/679
     * @throws IOException 
     */
    public void testHyphenatedRip() throws IOException {
        VscoRipper ripper = new VscoRipper(new URL("https://vsco.co/jolly-roger/images/1"));
        testRipper(ripper);
    }
    
    /**
     * Make sure it names the folder something sensible.
     * @throws IOException 
     */
    public void testGetGID() throws IOException {
        URL url = new URL("https://vsco.co/minijello/media/571cd612542220261a123441");
        
        VscoRipper ripper = new VscoRipper(url);
        
        assertEquals("Failed to get GID", "minijello/571cd", ripper.getGID(url));
    }
  
}
