
package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.RedditRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class RedditRipperTest extends RippersTest {

    @Test
    @Tag("flaky") // https://github.com/RipMeApp/ripme/issues/253
    public void testRedditSubredditRip() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(new URI("http://www.reddit.com/r/nsfw_oc").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky") // https://github.com/RipMeApp/ripme/issues/253
    public void testRedditSubredditTopRip() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(new URI("http://www.reddit.com/r/nsfw_oc/top?t=all").toURL());
        testRipper(ripper);
    }

    @Test
    @Disabled
    public void testRedditPostRip() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(
                new URI("http://www.reddit.com/r/UnrealGirls/comments/1ziuhl/in_class_veronique_popa/").toURL());
        testRipper(ripper);
    }

    /**testRedditSubredditRip:19
     * GFYCAT TEST Tests a good GfycatURL (no "/gifs/detail")
     * 
     * @throws IOException
     */
    @Test
    @Tag("flaky")
    public void testRedditGfyGoodURL() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(
                new URI("https://www.reddit.com/r/bottesting/comments/7msozf/good_link/").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testSelfPostRip() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(
                new URI("https://www.reddit.com/r/gonewildstories/comments/oz7d97/f_18_finally_having_a_normal_sex_life/").toURL()
        );
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testSelfPostAuthorRip() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(new URI("https://www.reddit.com/user/ickybabie_").toURL());
        testRipper(ripper);
    }

    /**
     * GFYCAT TEST Tests a Bad URL with the "/gifs/detail" inside.
     * 
     * @throws IOException
     */
    @Test
    @Tag("flaky")
    public void testRedditGfyBadURL() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(
                new URI("https://www.reddit.com/r/bottesting/comments/7msmhi/bad_link/").toURL());
        testRipper(ripper);
    }

    /**
     * GFYCAT TEST Tests a gfycat URL with the gifdeliverynetwork/redgifs hosted video
     *
     * @throws IOException
     */
    @Test
    public void testRedditGfycatRedirectURL() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(
                new URI("https://www.reddit.com/r/NSFW_GIF/comments/ennwsa/gorgeous_tits/").toURL());
    }

    @Test
    @Tag("flaky")
    public void testRedditGallery() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(
                new URI("https://www.reddit.com/gallery/hrrh23").toURL());
        testRipper(ripper);
    }
}
