package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.rippers.NsfwAlbumRipper;

public class NsfwAlbumRipperTest extends RippersTest {
    @Test
    public void testNsfwAlbum1() throws IOException, URISyntaxException {
        NsfwAlbumRipper ripper = new NsfwAlbumRipper(new URI("https://nsfwalbum.com/album/905816").toURL());
        testRipper(ripper);
    }

    @Test
    public void testNsfwAlbum2() throws IOException, URISyntaxException {
        NsfwAlbumRipper ripper = new NsfwAlbumRipper(new URI("https://nsfwalbum.com/album/850951").toURL());
        testRipper(ripper);
    }
}
