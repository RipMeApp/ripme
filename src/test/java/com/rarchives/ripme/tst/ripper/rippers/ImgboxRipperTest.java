package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ImgboxRipper;

public class ImgboxRipperTest extends RippersTest {
    public void testImgboxRip() throws IOException {
        ImgboxRipper ripper = new ImgboxRipper(new URL("https://imgbox.com/g/FJPF7t26FD"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://imgbox.com/g/FJPF7t26FD");
        ImgboxRipper ripper = new ImgboxRipper(url);
        assertEquals("FJPF7t26FD", ripper.getGID(url));
    }
}
