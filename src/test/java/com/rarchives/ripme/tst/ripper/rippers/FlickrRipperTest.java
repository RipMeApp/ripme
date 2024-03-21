package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.FlickrRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class FlickrRipperTest extends RippersTest {
    @Test
    @Tag("slow")
    public void testFlickrAlbum() throws IOException, URISyntaxException {
        FlickrRipper ripper = new FlickrRipper(
                new URI("https://www.flickr.com/photos/leavingallbehind/sets/72157621895942720/").toURL());
        testRipper(ripper);
    }

}
