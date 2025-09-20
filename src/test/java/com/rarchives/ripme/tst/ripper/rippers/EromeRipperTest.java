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
    // Note: this album has been deleted, but the GID can still be extracted
    public static final String DELETED_ALBUM_GID_TEST = "https://www.erome.com/a/KbDAM1XT";

    // User page with 2 video album posts
    public static final String USER_PAGE_JAY_JENNA = "https://www.erome.com/Jay-Jenna";

    // 35 photos, no videos
    public static final String SHOKO_TAKAHASHI_PHOTO_ALBUM = "https://www.erome.com/a/Tak8F2h6";

    // no photos, 1 video
    public static final String VIDEO_ALBUM_SINGLE_VIDEO_RIDING = "https://www.erome.com/a/P0x5Ambn";

    // no photos, 2 videos
    public static final String VIDEO_ALBUM_MULTI_VIDEO_FUN_AT_SEA = "https://www.erome.com/a/jEUFu6pi";

    // 2 photos, 1 video, video is the last item
    public static final String VIDEO_PHOTO_ALBUM_VIDEO_LAST_THICK_ASIAN = "https://www.erome.com/a/4EqqN5LR";

    // 1 video, 1 photo, video is the first item
    public static final String VIDEO_PHOTO_ALBUM_VIDEO_FIRST_ARGENTINA = "https://www.erome.com/a/Stjsocxo";

    @Test
    public void testGetGIDAlbum() throws IOException, URISyntaxException {
        // Note: this album has been deleted, but the GID can still be extracted
        URL url = new URI(DELETED_ALBUM_GID_TEST).toURL();
        EromeRipper ripper = new EromeRipper(url);
        Assertions.assertEquals("KbDAM1XT", ripper.getGID(url));
    }

    @Test
    public void testGetGIDProfilePage() throws IOException, URISyntaxException {
        URL url = new URI(USER_PAGE_JAY_JENNA).toURL();
        EromeRipper ripper = new EromeRipper(url);
        Assertions.assertEquals("Jay-Jenna", ripper.getGID(url));
    }

    @Test
    public void testGetAlbumsToQueue() throws IOException, URISyntaxException {
        // User page with 2 video album posts
        URL url = new URI(USER_PAGE_JAY_JENNA).toURL();
        EromeRipper ripper = new EromeRipper(url);
        assert (2 >= ripper.getAlbumsToQueue(ripper.getFirstPage()).size());
    }

    @Test
    public void testPageContainsAlbums() throws IOException, URISyntaxException {
        URL url = new URI(USER_PAGE_JAY_JENNA).toURL();
        EromeRipper ripper = new EromeRipper(url);
        assert (ripper.pageContainsAlbums(url));
    }

    @Test
    public void testEmptyPageDoesNotContainAlbums() throws IOException, URISyntaxException {
        URL url = new URI(DELETED_ALBUM_GID_TEST).toURL();
        EromeRipper ripper = new EromeRipper(url);
        assert (!ripper.pageContainsAlbums(url));
    }

    @Test
    public void testGetURLsFromPhotoAlbumPage() throws IOException, URISyntaxException {
        // 35 photos, no videos
        URL url = new URI(SHOKO_TAKAHASHI_PHOTO_ALBUM).toURL();
        EromeRipper ripper = new EromeRipper(url);
        assert (35 == ripper.getURLsFromPage(ripper.getFirstPage()).size());
    }

    @Test
    public void testPhotoAlbumRip() throws IOException, URISyntaxException {
        // 35 photos, no videos
        URL url = new URI(SHOKO_TAKAHASHI_PHOTO_ALBUM).toURL();
        EromeRipper ripper = new EromeRipper(url);
        testRipper(ripper);
    }

    @Test
    @Tag("slow")
    public void testVideoAlbumWithSingleItemRip() throws IOException, URISyntaxException {
        // no photos, 1 video
        URL url = new URI(VIDEO_ALBUM_SINGLE_VIDEO_RIDING).toURL();
        EromeRipper ripper = new EromeRipper(url);
        testRipper(ripper);
    }

    @Test
    @Tag("slow")
    public void testVideoAlbumWithMultipleItemsRip() throws IOException, URISyntaxException {
        // no photos, 2 videos
        URL url = new URI(VIDEO_ALBUM_MULTI_VIDEO_FUN_AT_SEA).toURL();
        EromeRipper ripper = new EromeRipper(url);
        testRipper(ripper);
    }

    @Test
    @Tag("slow")
    public void testAlbumWithBothVideoLastRip() throws IOException, URISyntaxException {
        // 2 photos, 1 video, video is the last item
        URL url = new URI(VIDEO_PHOTO_ALBUM_VIDEO_LAST_THICK_ASIAN).toURL();
        EromeRipper ripper = new EromeRipper(url);
        testRipper(ripper);
    }

    @Test
    @Tag("slow")
    public void testAlbumWithBothVideoFirstRip() throws IOException, URISyntaxException {
        // 2 photos, 1 video
        URL url = new URI(VIDEO_PHOTO_ALBUM_VIDEO_FIRST_ARGENTINA).toURL();
        EromeRipper ripper = new EromeRipper(url);
        testRipper(ripper);
    }
}
