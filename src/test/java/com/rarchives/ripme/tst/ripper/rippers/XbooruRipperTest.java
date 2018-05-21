package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.XbooruRipper;

public class XbooruRipperTest extends RippersTest {
    public void testRip() throws IOException {
        XbooruRipper ripper = new XbooruRipper(new URL("http://xbooru.com/index.php?page=post&s=list&tags=furry"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("http://xbooru.com/index.php?page=post&s=list&tags=furry");
        XbooruRipper ripper = new XbooruRipper(url);
        assertEquals("furry", ripper.getGID(url));
    }
}