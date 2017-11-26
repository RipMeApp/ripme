package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.FuskatorRipper;

public class FuskatorRipperTest extends RippersTest {
    public void testFuskatorAlbum() throws IOException {
        FuskatorRipper ripper = new FuskatorRipper(new URL("http://fuskator.com/full/emJa1U6cqbi/index.html"));
        testRipper(ripper);
    }
}
