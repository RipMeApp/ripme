package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.VscoRipper;
import java.io.IOException;
import java.net.URL;

public class VscoRipperTest extends RippersTest {
    
    /**
     * Testing Rip.
     * @throws IOException 
     */
    public void testSingleImageRip() throws IOException{
        VscoRipper ripper = new VscoRipper(new URL("https://vsco.co/minijello/media/571cd612542220261a123441"));
        testRipper(ripper);
    }
    
    /**
     * Make sure it names the folder something sensible.
     * @throws IOException 
     */
    public void testGetGID() throws IOException{
        URL url = new URL("https://vsco.co/minijello/media/571cd612542220261a123441");
        
        VscoRipper ripper = new VscoRipper(url);
        
        assertEquals("Failed to get GID", "minijello/571cd", ripper.getGID(url));
    }
  
}
