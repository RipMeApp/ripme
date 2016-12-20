package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.rarchives.ripme.ripper.rippers.ImgurRipper;
import com.rarchives.ripme.ripper.rippers.ImgurRipper.ImgurAlbum;
import com.rarchives.ripme.utils.Utils;

public class ImgurRipperTest extends RippersTest {

    public void testImgurURLFailures() throws IOException {
        List<URL> failURLs = new ArrayList<URL>();
        // Imgur urls that should not work
        failURLs.add(new URL("http://imgur.com"));
        failURLs.add(new URL("http://imgur.com/"));
        failURLs.add(new URL("http://i.imgur.com"));
        failURLs.add(new URL("http://i.imgur.com/"));
        failURLs.add(new URL("http://imgur.com/image"));
        failURLs.add(new URL("http://imgur.com/image.jpg"));
        failURLs.add(new URL("http://i.imgur.com/image.jpg"));
        for (URL url : failURLs) {
            try {
                new ImgurRipper(url);
                fail("Instantiated ripper for URL that should not work: " + url);
            } catch (Exception e) {
                // Expected
                continue;
            }
        }
    }

    public void testImgurAlbums() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();
        // URLs that should return more than 1 image
        contentURLs.add(new URL("http://imgur.com/a/dS9OQ#0")); // Horizontal layout
        contentURLs.add(new URL("http://imgur.com/a/YpsW9#0")); // Grid layout
        contentURLs.add(new URL("http://imgur.com/a/WxG6f/layout/vertical#0"));
        contentURLs.add(new URL("http://imgur.com/a/WxG6f/layout/horizontal#0"));
        contentURLs.add(new URL("http://imgur.com/a/WxG6f/layout/grid#0"));
        contentURLs.add(new URL("http://imgur.com/gallery/FmP2o")); // Gallery URL
        // Imgur seems not to support URLs with lists of images anymore.
        //contentURLs.add(new URL("http://imgur.com/758qD43,C6iVJex,bP7flAu,J3l85Ri,1U7fhu5,MbuAUCM,JF4vOXQ"));
        // Sometimes hangs up
        //contentURLs.add(new URL("http://imgur.com/r/nsfw_oc/top/all"));
        //contentURLs.add(new URL("http://imgur.com/a/bXQpH")); // Album with titles/descriptions
        for (URL url : contentURLs) {
            ImgurRipper ripper = new ImgurRipper(url);
            testRipper(ripper);
        }
    }


    public void testImgurAlbumWithMoreThan20Pictures() throws IOException {
        ImgurAlbum album = ImgurRipper.getImgurAlbum(new URL("http://imgur.com/a/HUMsq"));
        assertTrue("Failed to find 20 files from " + album.url.toExternalForm() + ", only got " + album.images.size(), album.images.size() >= 20);
    }

    public void testImgurAlbumWithMoreThan100Pictures() throws IOException {
        ImgurAlbum album = ImgurRipper.getImgurAlbum(new URL("http://imgur.com/a/zXZBU"));
        assertTrue("Failed to find 100 files from " + album.url.toExternalForm() + ", only got " + album.images.size(), album.images.size() >= 100);
    }

    /*
    // Imgur seems to be really flaky with this huge album, or the album was removed or something.
    // Navigating to this link results in an "over capacity" warning on the page.
    // I wonder if our testing automation is what is putting this album over capacity?
    // See issue #376.
    public void testImgurAlbumWithMoreThan1000Pictures() throws IOException {
        ImgurAlbum album = ImgurRipper.getImgurAlbum(new URL("http://imgur.com/a/vsuh5"));
        assertTrue("Failed to find 1000 files from " + album.url.toExternalForm() + ", only got " + album.images.size(), album.images.size() >= 1000);
    }
    */
}
