package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.TwitterRipper;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class TwitterRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testTwitterUserRip() throws IOException {
        TwitterRipper ripper = new TwitterRipper(new URL("https://twitter.com/danngamber01/media"));
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testTwitterSearchRip() throws IOException {
        TwitterRipper ripper = new TwitterRipper(
                new URL("https://twitter.com/search?f=tweets&q=from%3Aalinalixxx%20filter%3Aimages&src=typd"));
        testRipper(ripper);
    }

}
