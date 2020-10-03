package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.HentaiNexusRipper;
import org.junit.jupiter.api.Test;

public class HentainexusRipperTest extends RippersTest {
    @Test
    public void testHentaiNexusAlbum() throws IOException {
        HentaiNexusRipper ripper = new HentaiNexusRipper(new URL("https://hentainexus.com/view/44"));
        testRipper(ripper);
    }
}
