package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.TheyiffgalleryRipper;

public class TheyiffgalleryRipperTest extends RippersTest {
    public void testTheyiffgallery() throws IOException {
        TheyiffgalleryRipper ripper = new TheyiffgalleryRipper(new URL("https://theyiffgallery.com/index?/category/4303"));
        testRipper(ripper);
    }
}
