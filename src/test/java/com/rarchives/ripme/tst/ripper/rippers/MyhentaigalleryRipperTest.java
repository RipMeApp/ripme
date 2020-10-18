package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.MyhentaigalleryRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MyhentaigalleryRipperTest extends RippersTest {
    @Test
    public void testMyhentaigalleryAlbum() throws IOException {
        MyhentaigalleryRipper ripper = new MyhentaigalleryRipper(
                new URL("https://myhentaigallery.com/gallery/thumbnails/9201"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://myhentaigallery.com/gallery/thumbnails/9201");
        MyhentaigalleryRipper ripper = new MyhentaigalleryRipper(url);
        Assertions.assertEquals("9201", ripper.getGID(url));
    }
}