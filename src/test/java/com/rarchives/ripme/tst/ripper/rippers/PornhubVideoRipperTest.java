package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.video.PornhubRipper;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author Bob Zhang
 */
public class PornhubVideoRipperTest extends RippersTest{
    
    public void testCanRipTrue() throws IOException{
        
        PornhubRipper ripper = new PornhubRipper(new URL("https://www.pornhub.com/view_video.php?viewkey=ph5a65e6760d7ed"));
        URL toRip = new URL("https://www.pornhub.com/view_video.php?viewkey=ph5a65e6760d7ed");
        
        assertTrue(ripper.canRip(toRip));
    }
    
    public void testCanRipFalse() throws IOException{
        
        PornhubRipper ripper = new PornhubRipper(new URL("https://www.pornhub.com/view_video.php?viewkey=ph5a65e6760d7ed"));
        URL toRip = new URL("https://www.pornhub.com/view_video.php?viewkeysss=ph5a65e6760d7ed");
        
        assertFalse(ripper.canRip(toRip));
    }
    
    public void testRip() throws IOException{
        URL toRip = new URL("https://www.pornhub.com/view_video.php?viewkey=ph5a6d8f368249b");
        PornhubRipper ripper = new PornhubRipper(toRip);
        testRipper(ripper);
    }
    
}
