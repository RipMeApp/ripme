package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.MyhentaigalleryRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class MyhentaigalleryRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testMyhentaigalleryAlbum() throws IOException, URISyntaxException {
        MyhentaigalleryRipper ripper = new MyhentaigalleryRipper(
                new URI("https://myhentaigallery.com/gallery/thumbnails/9201").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://myhentaigallery.com/gallery/thumbnails/9201").toURL();
        MyhentaigalleryRipper ripper = new MyhentaigalleryRipper(url);
        Assertions.assertEquals("9201", ripper.getGID(url));
    }
}