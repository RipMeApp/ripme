package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.rippers.FreeComicOnlineRipper;

public class FreeComicOnlineRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testFreeComicOnlineChapterAlbum() throws IOException, URISyntaxException {
        FreeComicOnlineRipper ripper = new FreeComicOnlineRipper(
                new URI("https://freecomiconline.me/comic/dungeon-architect/chapter-01/").toURL());
        testRipper(ripper);
    }
}
