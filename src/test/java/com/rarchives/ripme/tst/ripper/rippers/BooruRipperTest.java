package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.BooruRipper;

public class BooruRipperTest extends RippersTest {
    public void testRip() throws IOException {
        BooruRipper ripper = new BooruRipper(new URL("http://xbooru.com/index.php?page=post&s=list&tags=furry"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("http://xbooru.com/index.php?page=post&s=list&tags=furry");
        BooruRipper ripper = new BooruRipper(url);
        assertEquals("furry", ripper.getGID(url));
    }
}