package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ImgboxRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class ImgboxRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testImgboxRip() throws IOException, URISyntaxException {
        ImgboxRipper ripper = new ImgboxRipper(new URI("https://imgbox.com/g/FJPF7t26FD").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://imgbox.com/g/FJPF7t26FD").toURL();
        ImgboxRipper ripper = new ImgboxRipper(url);
        Assertions.assertEquals("FJPF7t26FD", ripper.getGID(url));
    }
}
