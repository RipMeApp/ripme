package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.rippers.EromeRipper;

public class EromeRipperTest extends RippersTest {
    @Test
    public void testGetGIDProfilePage() throws IOException, URISyntaxException {
        URL url = new URI("https://www.erome.com/Jay-Jenna").toURL();
        EromeRipper ripper = new EromeRipper(url);
        Assertions.assertEquals("Jay-Jenna", ripper.getGID(url));
    }

    @Test
    public void testGetGIDAlbum() throws IOException, URISyntaxException {
        URL url = new URI("https://www.erome.com/a/KbDAM1XT").toURL();
        EromeRipper ripper = new EromeRipper(url);
        Assertions.assertEquals("KbDAM1XT", ripper.getGID(url));
    }

    @Test
    public void testGetAlbumsToQueue() throws IOException, URISyntaxException {
        URL url = new URI("https://www.erome.com/Jay-Jenna").toURL();
        EromeRipper ripper = new EromeRipper(url);
        assert (2 >= ripper.getAlbumsToQueue(ripper.getFirstPage()).size());
    }

    @Test
    public void testPageContainsAlbums() throws IOException, URISyntaxException {
        URL url = new URI("https://www.erome.com/Jay-Jenna").toURL();
        EromeRipper ripper = new EromeRipper(url);
        assert (ripper.pageContainsAlbums(url));
        assert (!ripper.pageContainsAlbums(new URI("https://www.erome.com/a/KbDAM1XT").toURL()));
    }

    @Test
    public void testRip() throws IOException, URISyntaxException {
        URL url = new URI("https://www.erome.com/a/Tak8F2h6").toURL();
        EromeRipper ripper = new EromeRipper(url);
        testRipper(ripper);
    }

    @Test
    @Tag("slow")
    public void testVideoAlbumRip() throws IOException, URISyntaxException {
        URL url = new URI("https://www.erome.com/a/jEUFu6pi").toURL();
        EromeRipper ripper = new EromeRipper(url);
        testRipper(ripper);
    }

    @Test
    public void testGetURLsFromPage() throws IOException, URISyntaxException {
        URL url = new URI("https://www.erome.com/a/Tak8F2h6").toURL();
        EromeRipper ripper = new EromeRipper(url);
        assert (35 == ripper.getURLsFromPage(ripper.getFirstPage()).size());
    }
}
