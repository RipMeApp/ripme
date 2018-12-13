package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.HentaifoundryRipper;

public class HentaifoundryRipperTest extends RippersTest {
    public void testHentaifoundryRip() throws IOException {
        HentaifoundryRipper ripper = new HentaifoundryRipper(new URL("https://www.hentai-foundry.com/pictures/user/personalami"));
        testRipper(ripper);
    }

    public void testHentaifoundryPdfRip() throws IOException {
        HentaifoundryRipper ripper = new HentaifoundryRipper(new URL("https://www.hentai-foundry.com/stories/user/Rakked"));
        testRipper(ripper);
    }
}
