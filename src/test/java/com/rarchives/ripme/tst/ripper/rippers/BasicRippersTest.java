package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.rarchives.ripme.ripper.rippers.DeviantartRipper;
import com.rarchives.ripme.ripper.rippers.EightmusesRipper;
import com.rarchives.ripme.ripper.rippers.FineboxRipper;
import com.rarchives.ripme.ripper.rippers.GonewildRipper;
import com.rarchives.ripme.ripper.rippers.MotherlessRipper;
import com.rarchives.ripme.ripper.rippers.RedditRipper;
import com.rarchives.ripme.ripper.rippers.SeeniveRipper;
import com.rarchives.ripme.ripper.rippers.TumblrRipper;
import com.rarchives.ripme.ripper.rippers.TwitterRipper;
import com.rarchives.ripme.ripper.rippers.VkRipper;
import com.rarchives.ripme.ripper.rippers.XhamsterRipper;

/**
 * Simple test cases for various rippers.
 * These tests only require a URL, no other special validation.
 */
public class BasicRippersTest extends RippersTest {

    public void testMotherlessAlbums() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();

        // Image album
        contentURLs.add(new URL("http://motherless.com/G4DAA18D"));
        // Video album
        // XXX: Commented out because test takes too long to download the file.
        // contentURLs.add(new URL("http://motherless.com/GFD0F537"));

        for (URL url : contentURLs) {
            MotherlessRipper ripper = new MotherlessRipper(url);
            testRipper(ripper);
        }
    }

    public void testDeviantartAlbums() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();

        // Small gallery
        contentURLs.add(new URL("http://airgee.deviantart.com/gallery/"));
        // NSFW gallery
        contentURLs.add(new URL("http://faterkcx.deviantart.com/gallery/"));
        // Multi-page NSFW
        contentURLs.add(new URL("http://geekysica.deviantart.com/gallery/35209412"));

        for (URL url : contentURLs) {
            DeviantartRipper ripper = new DeviantartRipper(url);
            testRipper(ripper);
        }
    }

    public void testEightmusesAlbums() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();

        contentURLs.add(new URL("http://www.8muses.com/index/category/jab-hotassneighbor7"));

        for (URL url : contentURLs) {
            EightmusesRipper ripper = new EightmusesRipper(url);
            testRipper(ripper);
        }
    }

    public void testVineboxAlbums() throws IOException {
        Logger.getRootLogger().setLevel(Level.ALL);
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://vinebox.co/u/wi57hMjc2Ka"));
        contentURLs.add(new URL("http://finebox.co/u/wi57hMjc2Ka"));
        for (URL url : contentURLs) {
            FineboxRipper ripper = new FineboxRipper(url);
            testRipper(ripper);
        }
    }
    
    public void testXhamsterAlbums() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://xhamster.com/photos/gallery/1462237/alyssa_gadson.html"));
        contentURLs.add(new URL("http://xhamster.com/photos/gallery/2941201/tableau_d_039_art_ii.html"));
        for (URL url : contentURLs) {
            XhamsterRipper ripper = new XhamsterRipper(url);
            testRipper(ripper);
        }
    }

    public void testGonewildAlbums() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://gonewild.com/user/amle69"));
        for (URL url : contentURLs) {
            GonewildRipper ripper = new GonewildRipper(url);
            testRipper(ripper);
        }
    }

    public void testRedditAlbums() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://www.reddit.com/r/nsfw_oc"));
        contentURLs.add(new URL("http://www.reddit.com/r/nsfw_oc/top?t=all"));
        contentURLs.add(new URL("http://www.reddit.com/r/UnrealGirls/comments/1ziuhl/in_class_veronique_popa/"));
        for (URL url : contentURLs) {
            RedditRipper ripper = new RedditRipper(url);
            testRipper(ripper);
        }
    }
    
    public void testSeeniveAlbums() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://seenive.com/u/946491170220040192"));
        for (URL url : contentURLs) {
            SeeniveRipper ripper = new SeeniveRipper(url);
            testRipper(ripper);
        }
    }

    public void testTumblrAlbums() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://wrouinr.tumblr.com/archive"));
        contentURLs.add(new URL("http://topinstagirls.tumblr.com/tagged/berlinskaya"));
        contentURLs.add(new URL("http://genekellyclarkson.tumblr.com/post/86100752527/lucyannebrooks-rachaelboden-friends-goodtimes-bed-boobs"));
        for (URL url : contentURLs) {
            TumblrRipper ripper = new TumblrRipper(url);
            testRipper(ripper);
        }
    }

    public void testTwitterAlbums() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("https://twitter.com/danngamber01/media"));
        contentURLs.add(new URL("https://twitter.com/search?q=from%3Apurrbunny%20filter%3Aimages&src=typd"));
        for (URL url : contentURLs) {
            TwitterRipper ripper = new TwitterRipper(url);
            testRipper(ripper);
        }
    }
    
    public void testVkAlbum() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("https://vk.com/album45506334_172415053"));
        contentURLs.add(new URL("https://vk.com/album45506334_0"));
        contentURLs.add(new URL("https://vk.com/photos45506334"));
        for (URL url : contentURLs) {
            VkRipper ripper = new VkRipper(url);
            testRipper(ripper);
        }
    }
}
