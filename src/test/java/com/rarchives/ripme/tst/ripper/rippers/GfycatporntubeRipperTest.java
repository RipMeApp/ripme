package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.GfycatporntubeRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class GfycatporntubeRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testRip() throws IOException {
        GfycatporntubeRipper ripper = new GfycatporntubeRipper(new URL("https://gfycatporntube.com/blowjob-bunny-puts-on-a-show/"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://gfycatporntube.com/blowjob-bunny-puts-on-a-show/");
        GfycatporntubeRipper ripper = new GfycatporntubeRipper(url);
        Assertions.assertEquals("blowjob-bunny-puts-on-a-show", ripper.getGID(url));
    }
}
