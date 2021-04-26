
package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.RedditRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class RedditRipperTest extends RippersTest {

    @Test
    @Tag("flaky") // https://github.com/RipMeApp/ripme/issues/253
    public void testRedditSubredditRip() throws IOException {
        RedditRipper ripper = new RedditRipper(new URL("http://www.reddit.com/r/nsfw_oc"));
        testRipper(ripper);
    }

    @Test
    @Tag("flaky") // https://github.com/RipMeApp/ripme/issues/253
    public void testRedditSubredditTopRip() throws IOException {
        RedditRipper ripper = new RedditRipper(new URL("http://www.reddit.com/r/nsfw_oc/top?t=all"));
        testRipper(ripper);
    }

    @Test
    @Disabled
    public void testRedditPostRip() throws IOException {
        RedditRipper ripper = new RedditRipper(
                new URL("http://www.reddit.com/r/UnrealGirls/comments/1ziuhl/in_class_veronique_popa/"));
        testRipper(ripper);
    }

    /**testRedditSubredditRip:19
     * GFYCAT TEST Tests a good GfycatURL (no "/gifs/detail")
     * 
     * @throws IOException
     */
    @Test
    @Tag("flaky")
    public void testRedditGfyGoodURL() throws IOException {
        RedditRipper ripper = new RedditRipper(
                new URL("https://www.reddit.com/r/bottesting/comments/7msozf/good_link/"));
        testRipper(ripper);
    }

    /**
     * GFYCAT TEST Tests a Bad URL with the "/gifs/detail" inside.
     * 
     * @throws IOException
     */
    @Test
    @Tag("flaky")
    public void testRedditGfyBadURL() throws IOException {
        RedditRipper ripper = new RedditRipper(
                new URL("https://www.reddit.com/r/bottesting/comments/7msmhi/bad_link/"));
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testRedditGallery() throws IOException{
        RedditRipper ripper = new RedditRipper(
                new URL("https://www.reddit.com/gallery/hrrh23"));
        testRipper(ripper);
    }
}
