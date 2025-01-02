package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.HentaifoxRipper;

public class HentaifoxRipperTest extends RippersTest {
    public void testRip() throws IOException, URISyntaxException {
        HentaifoxRipper ripper = new HentaifoxRipper(new URI("https://hentaifox.com/gallery/38544/").toURL());
        testRipper(ripper);
    }
}
