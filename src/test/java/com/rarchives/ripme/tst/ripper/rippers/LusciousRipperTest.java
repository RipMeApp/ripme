package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.LusciousRipper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class LusciousRipperTest extends RippersTest {
    @Test
    public void testPahealRipper() throws IOException {
        // a photo set
        LusciousRipper ripper = new LusciousRipper(
                new URL("https://luscious.net/albums/h-na-alice-wa-suki-desu-ka-do-you-like-alice-when_321609/"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://luscious.net/albums/h-na-alice-wa-suki-desu-ka-do-you-like-alice-when_321609/");
        LusciousRipper ripper = new LusciousRipper(url);
        assertEquals("h-na-alice-wa-suki-desu-ka-do-you-like-alice-when_321609", ripper.getGID(url));
    }
    
    @Test
    public void testGetNextPage() throws IOException {
        URL multiPageAlbumUrl = new URL("https://luscious.net/albums/women-of-color_58/");
        LusciousRipper multiPageRipper = new LusciousRipper(multiPageAlbumUrl);
        assert (multiPageRipper.getNextPage(multiPageRipper.getFirstPage()) != null);

        URL singlePageAlbumUrl = new URL("https://members.luscious.net/albums/bakaneko-navidarks_332097/");
        LusciousRipper singlePageRipper = new LusciousRipper(singlePageAlbumUrl);
        try {
            singlePageRipper.getNextPage(singlePageRipper.getFirstPage());
        } catch (IOException e) {
            assertEquals("No next page found.", e.getMessage());
        }
    }
}