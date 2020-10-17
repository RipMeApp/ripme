package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.HentaifoundryRipper;
<<<<<<< HEAD
=======
import org.junit.jupiter.api.Assertions;
>>>>>>> upstream/master
import org.junit.jupiter.api.Test;

public class HentaifoundryRipperTest extends RippersTest {
    @Test
    public void testHentaifoundryRip() throws IOException {
        HentaifoundryRipper ripper = new HentaifoundryRipper(new URL("https://www.hentai-foundry.com/pictures/user/personalami"));
        testRipper(ripper);
    }
    @Test
    public void testHentaifoundryGetGID() throws IOException {
        HentaifoundryRipper ripper = new HentaifoundryRipper(new URL("https://www.hentai-foundry.com/stories/user/Rakked"));
        testRipper(ripper);
<<<<<<< HEAD
        assertEquals("Rakked", ripper.getGID(new URL("https://www.hentai-foundry.com/stories/user/Rakked")));
=======
        Assertions.assertEquals("Rakked", ripper.getGID(new URL("https://www.hentai-foundry.com/stories/user/Rakked")));
>>>>>>> upstream/master
    }
    @Test
    public void testHentaifoundryPdfRip() throws IOException {
        HentaifoundryRipper ripper = new HentaifoundryRipper(new URL("https://www.hentai-foundry.com/stories/user/Rakked"));
        testRipper(ripper);
    }
}
