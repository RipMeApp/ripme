package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.MyhentaicomicsRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class MyhentaicomicsRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testMyhentaicomicsAlbum() throws IOException, URISyntaxException {
        MyhentaicomicsRipper ripper = new MyhentaicomicsRipper(new URI("http://myhentaicomics.com/index.php/Nienna-Lost-Tales").toURL());
        testRipper(ripper);
    }

    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("http://myhentaicomics.com/index.php/Nienna-Lost-Tales").toURL();
        MyhentaicomicsRipper ripper = new MyhentaicomicsRipper(url);
        // Test a comic
        Assertions.assertEquals("Nienna-Lost-Tales", ripper.getGID(url));
        // Test a search
        Assertions.assertEquals("test", ripper.getGID(new URI("http://myhentaicomics.com/index.php/search?q=test").toURL()));
        // Test a tag
        Assertions.assertEquals("2409", ripper.getGID(new URI("http://myhentaicomics.com/index.php/tag/2409/").toURL()));
    }
    @Test
    @Tag("flaky")
    public void testGetAlbumsToQueue() throws IOException, URISyntaxException {
        URL url = new URI("https://myhentaicomics.com/index.php/tag/3167/").toURL();
        MyhentaicomicsRipper ripper = new MyhentaicomicsRipper(url);
        Assertions.assertEquals(15, ripper.getAlbumsToQueue(ripper.getFirstPage()).size());
    }
    @Test
    public void testPageContainsAlbums() throws IOException, URISyntaxException {
        URL url = new URI("https://myhentaicomics.com/index.php/tag/3167/").toURL();
        URL url2 = new URI("https://myhentaicomics.com/index.php/search?q=test").toURL();
        MyhentaicomicsRipper ripper = new MyhentaicomicsRipper(url);
        Assertions.assertTrue(ripper.pageContainsAlbums(url));
        Assertions.assertTrue(ripper.pageContainsAlbums(url2));
    }
}