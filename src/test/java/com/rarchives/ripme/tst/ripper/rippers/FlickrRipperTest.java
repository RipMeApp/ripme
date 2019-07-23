package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.FlickrRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FlickrRipperTest extends RippersTest {
    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/243")
    public void testFlickrAlbum() throws IOException {
        FlickrRipper ripper = new FlickrRipper(
                new URL("https://www.flickr.com/photos/leavingallbehind/sets/72157621895942720/"));
        testRipper(ripper);
    }

}
