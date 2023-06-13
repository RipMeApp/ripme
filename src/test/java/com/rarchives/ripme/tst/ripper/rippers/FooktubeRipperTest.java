package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.FooktubeRipper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FooktubeRipperTest extends RippersTest {
    @Test
    @Disabled("test or ripper broken")
    public void testFooktubeVideo() throws IOException, URISyntaxException {
        FooktubeRipper ripper = new FooktubeRipper(new URI("https://fooktube.com/video/641/in-the-cinema").toURL());  //pick any video from the front page
        testRipper(ripper);
    }

}
