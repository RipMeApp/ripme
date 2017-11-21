package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.TwitterRipper;

public class TwitterRipperTest extends RippersTest {
    public void testTwitterUserRip() throws IOException {
        TwitterRipper ripper = new TwitterRipper(new URL("https://twitter.com/danngamber01/media"));
        testRipper(ripper);
    }

    /*
    public void testTwitterSearchRip() throws IOException {
        TwitterRipper ripper = new TwitterRipper(new URL("https://twitter.com/search?q=from%3ADaisyfairymfc%20filter%3Aimages&src=typd"));
        testRipper(ripper);
    }
    */
}
