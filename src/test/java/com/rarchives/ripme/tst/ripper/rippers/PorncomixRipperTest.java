package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.PorncomixRipper;

public class PorncomixRipperTest extends RippersTest {
    public void testPorncomixAlbum() throws IOException, URISyntaxException {
        PorncomixRipper ripper = new PorncomixRipper(new URI("http://www.porncomix.info/lust-unleashed-desire-to-submit/").toURL());
        testRipper(ripper);
    }
}