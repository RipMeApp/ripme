package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.YuvutuRipper;

public class YuvutuRipperTest extends RippersTest {

    public void testYuvutuAlbum1() throws IOException {
        YuvutuRipper ripper = new YuvutuRipper(new URL("http://www.yuvutu.com/modules.php?name=YuGallery&action=view&set_id=127013"));
        testRipper(ripper);
    }

    public void testYuvutuAlbum2() throws IOException {
        YuvutuRipper ripper = new YuvutuRipper(new URL("http://www.yuvutu.com/modules.php?name=YuGallery&action=view&set_id=420333"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("http://www.yuvutu.com/modules.php?name=YuGallery&action=view&set_id=420333");
        YuvutuRipper ripper = new YuvutuRipper(url);
        assertEquals("420333", ripper.getGID(url));
    }
}
