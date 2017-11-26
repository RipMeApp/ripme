package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.HentaifoundryRipper;

public class HentaifoundryRipperTest extends RippersTest {
    public void testHentaifoundryRip() throws IOException {
        HentaifoundryRipper ripper = new HentaifoundryRipper(new URL("http://www.hentai-foundry.com/pictures/user/personalami"));
        testRipper(ripper);
    }
}
