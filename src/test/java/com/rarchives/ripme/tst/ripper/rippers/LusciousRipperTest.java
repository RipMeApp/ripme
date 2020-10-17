package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.LusciousRipper;
<<<<<<< HEAD
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class LusciousRipperTest extends RippersTest {
    @Test @Disabled("Flaky in the CI")
=======
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LusciousRipperTest extends RippersTest {
    @Test
>>>>>>> upstream/master
    public void testPahealRipper() throws IOException {
        // a photo set
        LusciousRipper ripper = new LusciousRipper(
                new URL("https://luscious.net/albums/h-na-alice-wa-suki-desu-ka-do-you-like-alice-when_321609/"));
        testRipper(ripper);
    }

<<<<<<< HEAD
    public void testGetGID() throws IOException {
        URL url = new URL("https://luscious.net/albums/h-na-alice-wa-suki-desu-ka-do-you-like-alice-when_321609/");
        LusciousRipper ripper = new LusciousRipper(url);
        assertEquals("h-na-alice-wa-suki-desu-ka-do-you-like-alice-when_321609", ripper.getGID(url));
    }
    @Test @Disabled("Flaky in the CI")
=======
    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://luscious.net/albums/h-na-alice-wa-suki-desu-ka-do-you-like-alice-when_321609/");
        LusciousRipper ripper = new LusciousRipper(url);
        Assertions.assertEquals("h-na-alice-wa-suki-desu-ka-do-you-like-alice-when_321609", ripper.getGID(url));
    }
    
    @Test
>>>>>>> upstream/master
    public void testGetNextPage() throws IOException {
        URL multiPageAlbumUrl = new URL("https://luscious.net/albums/women-of-color_58/");
        LusciousRipper multiPageRipper = new LusciousRipper(multiPageAlbumUrl);
        assert (multiPageRipper.getNextPage(multiPageRipper.getFirstPage()) != null);

        URL singlePageAlbumUrl = new URL("https://members.luscious.net/albums/bakaneko-navidarks_332097/");
        LusciousRipper singlePageRipper = new LusciousRipper(singlePageAlbumUrl);
        try {
            singlePageRipper.getNextPage(singlePageRipper.getFirstPage());
        } catch (IOException e) {
<<<<<<< HEAD
            assertEquals("No next page found.", e.getMessage());
=======
            Assertions.assertEquals("No next page found.", e.getMessage());
>>>>>>> upstream/master
        }
    }
}