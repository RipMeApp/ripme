package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.EroShareRipper;
import com.rarchives.ripme.ripper.rippers.RedditRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class EroShareRipperTest extends RippersTest {

    // single image posts
    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/306 : EroShareRipper broken (even for eroshae links)")
    public void testImageEroshareFromRedditRip() throws IOException {
        RedditRipper ripper = new RedditRipper(new URL(
                "https://www.reddit.com/r/BestOfEroshare/comments/5z7foo/good_morning_who_likes_abstract_asian_artwork_f/"));
        testRipper(ripper);
    }

    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/306 : EroShareRipper broken (even for eroshae links)")
    public void testImageEroshareRip() throws IOException {
        EroShareRipper ripper = new EroShareRipper(new URL("https://eroshare.com/i/5j2qln3f"));
        testRipper(ripper);
    }

    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/306 : EroShareRipper broken (even for eroshae links)")
    public void testImageEroshaeRip() throws IOException {
        EroShareRipper ripper = new EroShareRipper(new URL("https://eroshae.com/i/5j2qln3f"));
        testRipper(ripper);
    }

    // video album post
    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/306 : EroShareRipper broken (even for eroshae links)")
    public void testVideoAlbumFromRedditRip() throws IOException {
        EroShareRipper ripper = new EroShareRipper(new URL(
                "https://www.reddit.com/r/BestOfEroshare/comments/5vyfnw/asian_mf_heard_i_should_post_here_date_night_her/"));
        testRipper(ripper);
    }

    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/306 : EroShareRipper broken (even for eroshae links)")
    public void testVideoAlbumEroshareRip() throws IOException {
        EroShareRipper ripper = new EroShareRipper(new URL("https://eroshare.com/wqnl6f00"));
        testRipper(ripper);
    }

    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/306 : EroShareRipper broken (even for eroshae links)")
    public void testVideoAlbumEroshaeRip() throws IOException {
        EroShareRipper ripper = new EroShareRipper(new URL("https://eroshae.com/wqnl6f00"));
        testRipper(ripper);
    }
}
