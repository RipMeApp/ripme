package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.E621Ripper;

public class E621RipperTest extends RippersTest {
    public void testRip() throws IOException {
        E621Ripper ripper = new E621Ripper(new URL("https://e621.net/post/index/1/beach"));
        testRipper(ripper);
    }
}
