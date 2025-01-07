package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.HentaiimageRipper;
import com.rarchives.ripme.utils.Utils;
import org.junit.jupiter.api.Test;

public class HentaiimageRipperTest extends RippersTest {
    @Test
    public void testHentaifoundryRip() throws IOException, URISyntaxException {
        if (Utils.getConfigBoolean("test.run_flaky_tests", false)) {
            HentaiimageRipper ripper = new HentaiimageRipper(new URI("https://hentai-image.com/image/afrobull-gerudo-ongoing-12/").toURL());
            testRipper(ripper);
        }
    }
}

