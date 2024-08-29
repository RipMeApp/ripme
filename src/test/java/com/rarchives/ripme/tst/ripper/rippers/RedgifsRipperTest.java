package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.RedditRipper;
import com.rarchives.ripme.ripper.rippers.RedgifsRipper;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class RedgifsRipperTest extends RippersTest {

    /**
     * Rips correctly formatted URL directly from Redgifs
     */
    @Test
    public void testRedgifsGoodURL() throws IOException, URISyntaxException {
        RedgifsRipper ripper = new RedgifsRipper(new URI("https://www.redgifs.com/watch/ashamedselfishcoypu").toURL());
        testRipper(ripper);
    }

    /**
     * Rips gifdeliverynetwork URL's by redirecting them to proper redgifs url
     */
    @Test
    public void testRedgifsBadRL() throws IOException, URISyntaxException {
        RedgifsRipper ripper = new RedgifsRipper(new URI("https://www.gifdeliverynetwork.com/consideratetrustworthypigeon").toURL());
        testRipper(ripper);
    }

    /**
     * Rips a Redgifs profile
     */
    @Test
    public void testRedgifsProfile() throws IOException, URISyntaxException {
        RedgifsRipper ripper  = new RedgifsRipper(new URI("https://www.redgifs.com/users/ra-kunv2").toURL());
        testRipper(ripper);
    }

    /**
     * Rips a Redgif search
     * @throws IOException
     */
    @Test
    public void testRedgifsSearch() throws IOException, URISyntaxException {
        RedgifsRipper ripper  = new RedgifsRipper(new URI("https://www.redgifs.com/search?query=take+a+shot+every+time").toURL());
        testRipper(ripper);
    }

    /**
     * Rips Redgif tags
     * @throws IOException
     */
    @Test
    public void testRedgifsTags() throws IOException, URISyntaxException {
        RedgifsRipper ripper  = new RedgifsRipper(new URI("https://www.redgifs.com/gifs/animation,sfw,funny?order=best&tab=gifs").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testRedditRedgifs() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(new URI("https://www.reddit.com/r/nsfwhardcore/comments/ouz5bw/me_cumming_on_his_face/").toURL());
        testRipper(ripper);
    }
}
