package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.HentaiimageRipper;
import com.rarchives.ripme.utils.Utils;

public class HentaiimageRipperTest extends RippersTest {

    public void testHentaifoundryRip() throws IOException {
        if (Utils.getConfigBoolean("test.run_flaky_tests", false)) {
            HentaiimageRipper ripper = new HentaiimageRipper(new URL("https://hentai-image.com/image/afrobull-gerudo-ongoing-12/"));
            testRipper(ripper);
        }
    }
}

