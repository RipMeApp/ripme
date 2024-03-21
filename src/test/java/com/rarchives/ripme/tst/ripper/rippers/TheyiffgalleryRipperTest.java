package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.TheyiffgalleryRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class TheyiffgalleryRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testTheyiffgallery() throws IOException, URISyntaxException {
        TheyiffgalleryRipper ripper = new TheyiffgalleryRipper(new URI("https://theyiffgallery.com/index?/category/4303").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://theyiffgallery.com/index?/category/4303").toURL();
        TheyiffgalleryRipper ripper = new TheyiffgalleryRipper(url);
        Assertions.assertEquals("4303", ripper.getGID(url));
    }
}
