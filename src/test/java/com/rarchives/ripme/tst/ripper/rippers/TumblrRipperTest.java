
package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.TumblrRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TumblrRipperTest extends RippersTest {
    @Test
    @Disabled
    public void testTumblrFullRip() throws IOException, URISyntaxException {
        TumblrRipper ripper = new TumblrRipper(new URI("http://wrouinr.tumblr.com").toURL());
        testRipper(ripper);
    }

    @Test
    @Disabled
    public void testTumblrTagRip() throws IOException, URISyntaxException {
        TumblrRipper ripper = new TumblrRipper(new URI("https://these-are-my-b-sides.tumblr.com/tagged/boobs").toURL());
        testRipper(ripper);
    }

    @Test
    @Disabled
    public void testTumblrPostRip() throws IOException, URISyntaxException {
        TumblrRipper ripper = new TumblrRipper(new URI("http://sadbaffoon.tumblr.com/post/132045920789/what-a-hoe").toURL());
        testRipper(ripper);
    }

    @Test
    @Disabled("Commented out because the test link is 404ing")
    public void testEmbeddedImage() throws IOException, URISyntaxException {
        TumblrRipper ripper = new TumblrRipper(
                new URI("https://these-are-my-b-sides.tumblr.com/post/178225921524/this-was-fun").toURL());
        testRipper(ripper);
    }
}
