package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.PorncomixRipper;

public class PorncomixRipperTest extends RippersTest {
    public void testPorncomixAlbum() throws IOException {
        PorncomixRipper ripper = new PorncomixRipper(new URL("http://www.porncomix.info/lust-unleashed-desire-to-submit/"));
        testRipper(ripper);
    }
}