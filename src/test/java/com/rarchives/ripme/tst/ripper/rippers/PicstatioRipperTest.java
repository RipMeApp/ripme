package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.PicstatioRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PicstatioRipperTest extends RippersTest {

    public void testRip() throws IOException, URISyntaxException {
        PicstatioRipper ripper = new PicstatioRipper(new URI("https://www.picstatio.com/aerial-view-wallpapers").toURL());
        testRipper(ripper);
    }
    @Test
    public void testGID() throws IOException, URISyntaxException {
        URL url = new URI("https://www.picstatio.com/aerial-view-wallpapers").toURL();
        PicstatioRipper ripper = new PicstatioRipper(url);
        Assertions.assertEquals("aerial-view-wallpapers", ripper.getGID(url));
    }
}