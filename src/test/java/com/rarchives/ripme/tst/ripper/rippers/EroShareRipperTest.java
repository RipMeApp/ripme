package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.EroShareRipper;
import com.rarchives.ripme.ripper.rippers.RedditRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class EroShareRipperTest extends RippersTest {

    // single image posts
    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/306 : EroShareRipper broken (even for eroshae links)")
    public void testImageEroshareFromRedditRip() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(new URI(
                "https://www.reddit.com/r/BestOfEroshare/comments/5z7foo/good_morning_who_likes_abstract_asian_artwork_f/").toURL());
        testRipper(ripper);
    }

    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/306 : EroShareRipper broken (even for eroshae links)")
    public void testImageEroshareRip() throws IOException, URISyntaxException {
        EroShareRipper ripper = new EroShareRipper(new URI("https://eroshare.com/i/5j2qln3f").toURL());
        testRipper(ripper);
    }

    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/306 : EroShareRipper broken (even for eroshae links)")
    public void testImageEroshaeRip() throws IOException, URISyntaxException {
        EroShareRipper ripper = new EroShareRipper(new URI("https://eroshae.com/i/5j2qln3f").toURL());
        testRipper(ripper);
    }

    // video album post
    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/306 : EroShareRipper broken (even for eroshae links)")
    public void testVideoAlbumFromRedditRip() throws IOException, URISyntaxException {
        EroShareRipper ripper = new EroShareRipper(new URI(
                "https://www.reddit.com/r/BestOfEroshare/comments/5vyfnw/asian_mf_heard_i_should_post_here_date_night_her/").toURL());
        testRipper(ripper);
    }

    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/306 : EroShareRipper broken (even for eroshae links)")
    public void testVideoAlbumEroshareRip() throws IOException, URISyntaxException {
        EroShareRipper ripper = new EroShareRipper(new URI("https://eroshare.com/wqnl6f00").toURL());
        testRipper(ripper);
    }

    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/306 : EroShareRipper broken (even for eroshae links)")
    public void testVideoAlbumEroshaeRip() throws IOException, URISyntaxException {
        EroShareRipper ripper = new EroShareRipper(new URI("https://eroshae.com/wqnl6f00").toURL());
        testRipper(ripper);
    }
}
