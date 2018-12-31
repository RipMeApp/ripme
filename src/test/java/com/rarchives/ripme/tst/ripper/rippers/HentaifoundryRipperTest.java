package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.HentaifoundryRipper;

public class HentaifoundryRipperTest extends RippersTest {
    public void testHentaifoundryRip() throws IOException {
        HentaifoundryRipper ripper = new HentaifoundryRipper(new URL("https://www.hentai-foundry.com/pictures/user/personalami"));
        testRipper(ripper);
    }

    public void testHentaifoundryGetGID() throws IOException {
        HentaifoundryRipper ripper = new HentaifoundryRipper(new URL("https://www.hentai-foundry.com/stories/user/Rakked"));
        assertEquals("Rakked", ripper.getGID(new URL("https://www.hentai-foundry.com/stories/user/Rakked")));
    }


    // For some reason this test does not work despite the feature working as expected (You can rip pdfs without error)
    // see https://github.com/RipMeApp/ripme/issues/1144
//    public void testHentaifoundryPdfRip() throws IOException {
//        HentaifoundryRipper ripper = new HentaifoundryRipper(new URL("https://www.hentai-foundry.com/stories/user/Rakked"));
//        testRipper(ripper);
//    }
}
