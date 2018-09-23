
package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.TumblrRipper;

public class TumblrRipperTest extends RippersTest {

    public void testTumblrFullRip() throws IOException {
        TumblrRipper ripper = new TumblrRipper(new URL("http://wrouinr.tumblr.com"));
        testRipper(ripper);
    }
    public void testTumblrTagRip() throws IOException {
        TumblrRipper ripper = new TumblrRipper(new URL("https://these-are-my-b-sides.tumblr.com/tagged/boobs"));
        testRipper(ripper);
    }
    public void testTumblrPostRip() throws IOException {
        TumblrRipper ripper = new TumblrRipper(new URL("http://sadbaffoon.tumblr.com/post/132045920789/what-a-hoe"));
        testRipper(ripper);
    }
    public void testEmbeddedImage() throws IOException {
        TumblrRipper ripper = new TumblrRipper(new URL("https://these-are-my-b-sides.tumblr.com/post/178225921524/this-was-fun"));
        testRipper(ripper);
    }
}
