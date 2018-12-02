package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.EromeRipper;

public class EromeRipperTest extends RippersTest {

    public void testGetGIDProfilePage() throws IOException {
        URL url = new URL("https://www.erome.com/Jay-Jenna");
        EromeRipper ripper = new EromeRipper(url);
        assertEquals("Jay-Jenna", ripper.getGID(url));
    }

    public void testGetGIDAlbum() throws IOException {
        URL url = new URL("https://www.erome.com/a/KbDAM1XT");
        EromeRipper ripper = new EromeRipper(url);
        assertEquals("KbDAM1XT", ripper.getGID(url));
    }

    public void testGetAlbumsToQueue() throws IOException {
        URL url = new URL("https://www.erome.com/Jay-Jenna");
        EromeRipper ripper = new EromeRipper(url);
        assert(2 >= ripper.getAlbumsToQueue(ripper.getFirstPage()).size());
    }

    public void testPageContainsAlbums() throws IOException {
        URL url = new URL("https://www.erome.com/Jay-Jenna");
        EromeRipper ripper = new EromeRipper(url);
        assert(ripper.pageContainsAlbums(url));
        assert(!ripper.pageContainsAlbums(new URL("https://www.erome.com/a/KbDAM1XT")));
    }

    public void testRip() throws IOException {
        URL url = new URL("https://www.erome.com/a/vlefBdsg");
        EromeRipper ripper = new EromeRipper(url);
        testRipper(ripper);
    }
}
