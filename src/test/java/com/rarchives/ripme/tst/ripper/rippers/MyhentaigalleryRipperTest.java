package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.MyhentaigalleryRipper;

public class MyhentaigalleryRipperTest extends RippersTest {

    public void testMyhentaigalleryAlbum() throws IOException {
        MyhentaigalleryRipper ripper = new MyhentaigalleryRipper(new URL("https://myhentaigallery.com/gallery/thumbnails/9201"));
        testRipper(ripper);
    }
}