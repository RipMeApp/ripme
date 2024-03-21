package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.TwitterRipper;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class TwitterRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testTwitterUserRip() throws IOException, URISyntaxException {
        TwitterRipper ripper = new TwitterRipper(new URI("https://twitter.com/danngamber01/media").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testTwitterSearchRip() throws IOException, URISyntaxException {
        TwitterRipper ripper = new TwitterRipper(
                new URI("https://twitter.com/search?f=tweets&q=from%3Aalinalixxx%20filter%3Aimages&src=typd").toURL());
        testRipper(ripper);
    }

}
