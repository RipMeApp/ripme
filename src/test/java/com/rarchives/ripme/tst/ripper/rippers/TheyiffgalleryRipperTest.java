package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.TheyiffgalleryRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TheyiffgalleryRipperTest extends RippersTest {
    @Test
    public void testTheyiffgallery() throws IOException {
        TheyiffgalleryRipper ripper = new TheyiffgalleryRipper(new URL("https://theyiffgallery.com/index?/category/4303"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://theyiffgallery.com/index?/category/4303");
        TheyiffgalleryRipper ripper = new TheyiffgalleryRipper(url);
        Assertions.assertEquals("4303", ripper.getGID(url));
    }
}
