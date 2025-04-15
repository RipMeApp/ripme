package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.NudeGalsRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NudeGalsRipperTest extends RippersTest {
    private static String ALBUM_TEST_URL = "https://nude-gals.com/photoshoot.php?photoshoot_id=5541";
    private static String VIDEO_TEST_URL = "https://nude-gals.com/video.php?video_id=1277";

    @Test
    public void testAlbumRip() throws IOException, URISyntaxException {
        NudeGalsRipper ripper = new NudeGalsRipper(new URI(ALBUM_TEST_URL).toURL());
        testRipper(ripper);
    }

    @Test
    public void testVideoRip() throws IOException, URISyntaxException {
        NudeGalsRipper ripper = new NudeGalsRipper(new URI(VIDEO_TEST_URL).toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetAlbumGID() throws IOException, URISyntaxException {
        NudeGalsRipper ripper = new NudeGalsRipper(new URI(ALBUM_TEST_URL).toURL());
        Assertions.assertEquals("album_5541", ripper.getGID( new URI(ALBUM_TEST_URL).toURL()));
    }

    @Test
    public void testGetVideoGID() throws IOException, URISyntaxException {
        NudeGalsRipper ripper = new NudeGalsRipper(new URI(VIDEO_TEST_URL).toURL());
        Assertions.assertEquals("video_1277", ripper.getGID(new URI(VIDEO_TEST_URL).toURL()));
    }
}
