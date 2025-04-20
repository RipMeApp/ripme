package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.rippers.HentaiimageRipper;

public class HentaiimageRipperTest extends RippersTest {
    @Test
    public void testHentaifoundryRip() throws IOException, URISyntaxException {
        HentaiimageRipper ripper = new HentaiimageRipper(
                new URI("https://hentai-img-xxx.com/image/afrobull-gerudo-ongoing-12/").toURL());
        testRipper(ripper);
    }
}
