package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.HentaiCafeRipper;

public class HentaicafeRipperTest extends RippersTest {
    public void testHentaiCafeAlbum() throws IOException {
        HentaiCafeRipper ripper = new HentaiCafeRipper(new URL("https://hentai.cafe/kikuta-the-oni-in-the-room/"));
        testRipper(ripper);
    }
}
