package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.GfycatporntubeRipper;

public class GfycatporntubeRipperTest extends RippersTest {
    public void testRip() throws IOException {
        GfycatporntubeRipper ripper = new GfycatporntubeRipper(new URL("https://gfycatporntube.com/blowjob-bunny-puts-on-a-show/"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://gfycatporntube.com/blowjob-bunny-puts-on-a-show/");
        GfycatporntubeRipper ripper = new GfycatporntubeRipper(url);
        assertEquals("blowjob-bunny-puts-on-a-show", ripper.getGID(url));
    }
}
