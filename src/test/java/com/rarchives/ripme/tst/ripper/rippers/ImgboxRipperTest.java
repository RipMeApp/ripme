package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ImgboxRipper;
<<<<<<< HEAD
=======
import org.junit.jupiter.api.Assertions;
>>>>>>> upstream/master
import org.junit.jupiter.api.Test;

public class ImgboxRipperTest extends RippersTest {
    @Test
    public void testImgboxRip() throws IOException {
        ImgboxRipper ripper = new ImgboxRipper(new URL("https://imgbox.com/g/FJPF7t26FD"));
        testRipper(ripper);
    }

<<<<<<< HEAD
    public void testGetGID() throws IOException {
        URL url = new URL("https://imgbox.com/g/FJPF7t26FD");
        ImgboxRipper ripper = new ImgboxRipper(url);
        assertEquals("FJPF7t26FD", ripper.getGID(url));
=======
    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://imgbox.com/g/FJPF7t26FD");
        ImgboxRipper ripper = new ImgboxRipper(url);
        Assertions.assertEquals("FJPF7t26FD", ripper.getGID(url));
>>>>>>> upstream/master
    }
}
