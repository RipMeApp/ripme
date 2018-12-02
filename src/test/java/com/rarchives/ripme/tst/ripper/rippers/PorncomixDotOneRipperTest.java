package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.PorncomixDotOneRipper;

public class PorncomixDotOneRipperTest extends RippersTest {
    public void testPorncomixAlbum() throws IOException {
        PorncomixDotOneRipper ripper = new PorncomixDotOneRipper(new URL("https://www.porncomix.one/gallery/blacknwhite-make-america-great-again"));
        testRipper(ripper);
    }
}