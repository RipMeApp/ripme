package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.HentaifoundryRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class HentaifoundryRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testHentaifoundryRip() throws IOException, URISyntaxException {
        HentaifoundryRipper ripper = new HentaifoundryRipper(new URI("https://www.hentai-foundry.com/pictures/user/personalami").toURL());
        testRipper(ripper);
    }
    @Test
    @Tag("flaky")
    public void testHentaifoundryGetGID() throws IOException, URISyntaxException {
        HentaifoundryRipper ripper = new HentaifoundryRipper(new URI("https://www.hentai-foundry.com/stories/user/Rakked").toURL());
        testRipper(ripper);
        Assertions.assertEquals("Rakked", ripper.getGID(new URI("https://www.hentai-foundry.com/stories/user/Rakked").toURL()));
    }
    @Test
    @Tag("flaky")
    public void testHentaifoundryPdfRip() throws IOException, URISyntaxException {
        HentaifoundryRipper ripper = new HentaifoundryRipper(new URI("https://www.hentai-foundry.com/stories/user/Rakked").toURL());
        testRipper(ripper);
    }
}
