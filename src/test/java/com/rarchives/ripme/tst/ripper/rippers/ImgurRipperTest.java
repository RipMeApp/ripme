package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.ImgurRipper;
import com.rarchives.ripme.ripper.rippers.ImgurRipper.ImgurAlbum;
import com.rarchives.ripme.utils.RipUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImgurRipperTest extends RippersTest {
    @Test
    public void testImgurURLFailures() throws IOException, URISyntaxException {
        List<URL> failURLs = new ArrayList<>();
        // Imgur urls that should not work
        failURLs.add(new URI("http://imgur.com").toURL());
        failURLs.add(new URI("http://imgur.com/").toURL());
        failURLs.add(new URI("http://i.imgur.com").toURL());
        failURLs.add(new URI("http://i.imgur.com/").toURL());
        failURLs.add(new URI("http://imgur.com/image.jpg").toURL());
        failURLs.add(new URI("http://i.imgur.com/image.jpg").toURL());
        // Imgur seems not to support URLs with lists of images anymore.
        failURLs.add(new URI("http://imgur.com/758qD43,C6iVJex,bP7flAu,J3l85Ri,1U7fhu5,MbuAUCM,JF4vOXQ").toURL());
        for (URL url : failURLs) {
            try {
                new ImgurRipper(url);
                Assertions.fail("Instantiated ripper for URL that should not work: " + url);
            } catch (Exception e) {
                // Expected
            }
        }
    }

    @Test
    public void testImgurAlbums() throws IOException, URISyntaxException {
        List<URL> contentURLs = new ArrayList<>();
        // URLs that should return more than 1 image
        contentURLs.add(new URI("http://imgur.com/gallery/FmP2o").toURL()); 
        // URLs with /gallery path
        contentURLs.add(new URI("http://imgur.com/gallery/nAl13J6").toURL()); 
        contentURLs.add(new URI("https://imgur.com/gallery/another-brendan-fraser-reaction-from-bedazzled-intergalactic-quality-nAl13J6").toURL()); 
        // URLs with /a path
        contentURLs.add(new URI("http://imgur.com/a/G058j5F").toURL()); 
        contentURLs.add(new URI("https://imgur.com/a/thanks-batman-G058j5F").toURL()); 
        contentURLs.add(new URI("https://imgur.com/a/thanks-batman-G058j5F/layout/grid#0").toURL()); 
        contentURLs.add(new URI("https://imgur.com/a/G058j5F/layout/grid#0").toURL()); 
        contentURLs.add(new URI("https://imgur.com/a/G058j5F/layout/horizontal#0").toURL()); 
        // Sometimes hangs up
        // contentURLs.add(new URI("http://imgur.com/r/nsfw_oc/top/all").toURL());
        // Album with titles/descriptions
        contentURLs.add(new URI("http://imgur.com/a/bXQpH").toURL()); 
        for (URL url : contentURLs) {
            ImgurRipper ripper = new ImgurRipper(url);
            testRipper(ripper);
        }
    }

    @Test
    public void testImgurUserAccount() throws IOException, URISyntaxException {
        List<String> contentURLs = new ArrayList<>();
        // URL with albums
        contentURLs.add("https://RockStarBrew.imgur.com");
        // New URL format
        contentURLs.add("https://imgur.com/user/RockStarBrew/");
        // And URL with images 
        contentURLs.add("https://imgur.com/user/counter2strike");
        for (var url : contentURLs) {
            ImgurRipper ripper = new ImgurRipper(new URI(url).toURL());
            testRipper(ripper);
        }
    }

    @Test
    public void testImgurSingleImage() throws IOException, URISyntaxException {
        List<URL> contentURLs = new ArrayList<>();
        contentURLs.add(new URI("http://imgur.com/qbfcLyG").toURL()); // Single image URL
        contentURLs.add(new URI("https://imgur.com/KexUO").toURL()); // Single image URL
        for (URL url : contentURLs) {
            ImgurRipper ripper = new ImgurRipper(url);
            testRipper(ripper);
        }
    }

    @Test
    public void testImgurAlbumWithMoreThan20Pictures() throws IOException, URISyntaxException {
        ImgurAlbum album = ImgurRipper.getImgurAlbum(new URI("http://imgur.com/a/HUMsq").toURL());
        Assertions.assertTrue(album.images.size() >= 20,
                "Failed to find 20 files from " + album.url.toExternalForm() + ", only got " + album.images.size());
    }

    @Test
    public void testImgurAlbumWithMoreThan100Pictures() throws IOException, URISyntaxException {
        ImgurAlbum album = ImgurRipper.getImgurAlbum(new URI("https://imgur.com/a/HX3JSrD").toURL());
        Assertions.assertTrue(album.images.size() >= 100,
                "Failed to find 100 files from " + album.url.toExternalForm() + ", only got " + album.images.size());
    }

    @Test
    public void testImgurVideoFromGetFilesFromURL() throws Exception {
        List<URL> urls = RipUtils.getFilesFromURL(new URI("https://i.imgur.com/7qoW0Mo.gifv").toURL());
        Assertions.assertEquals("https://i.imgur.com/7qoW0Mo.mp4", urls.get(0).toExternalForm());
    }

    /*
     * // Imgur seems to be really flaky with this huge album, or the album was
     * removed or something. // Navigating to this link results in an
     * "over capacity" warning on the page. // I wonder if our testing automation is
     * what is putting this album over capacity? // See issue #376. public void
     * testImgurAlbumWithMoreThan1000Pictures() throws IOException { ImgurAlbum
     * album = ImgurRipper.getImgurAlbum(new URI("http://imgur.com/a/vsuh5").toURL());
     * assertTrue("Failed to find 1000 files from " + album.url.toExternalForm() +
     * ", only got " + album.images.size(), album.images.size() >= 1000); }
     */
}
