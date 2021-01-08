package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.FooktubeRipper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FooktubeRipperTest extends RippersTest {
    @Test
    @Disabled("test or ripper broken")
    public void testFooktubeVideo() throws IOException {
        FooktubeRipper ripper = new FooktubeRipper(new URL("https://fooktube.com/video/641/in-the-cinema"));  //pick any video from the front page
        testRipper(ripper);
    }

}
