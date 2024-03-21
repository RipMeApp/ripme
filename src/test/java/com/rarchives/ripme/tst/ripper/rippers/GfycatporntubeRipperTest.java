package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.GfycatporntubeRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class GfycatporntubeRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testRip() throws IOException, URISyntaxException {
        GfycatporntubeRipper ripper = new GfycatporntubeRipper(new URI("https://gfycatporntube.com/blowjob-bunny-puts-on-a-show/").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://gfycatporntube.com/blowjob-bunny-puts-on-a-show/").toURL();
        GfycatporntubeRipper ripper = new GfycatporntubeRipper(url);
        Assertions.assertEquals("blowjob-bunny-puts-on-a-show", ripper.getGID(url));
    }
}
