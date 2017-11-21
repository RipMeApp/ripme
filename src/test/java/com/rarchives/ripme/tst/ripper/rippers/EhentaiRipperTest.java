package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.EHentaiRipper;

public class EhentaiRipperTest extends RippersTest {
    public void testEHentaiAlbum() throws IOException {
        EHentaiRipper ripper = new EHentaiRipper(new URL("https://e-hentai.org/g/1144492/e823bdf9a5/"));
        testRipper(ripper);
    }
}