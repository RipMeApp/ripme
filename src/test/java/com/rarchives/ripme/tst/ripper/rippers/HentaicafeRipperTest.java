package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.HentaiCafeRipper;
import org.junit.jupiter.api.Test;

public class HentaicafeRipperTest extends RippersTest {
    @Test
    public void testHentaiCafeAlbum() throws IOException {
        HentaiCafeRipper ripper = new HentaiCafeRipper(new URL("https://hentai.cafe/kikuta-the-oni-in-the-room/"));
        testRipper(ripper);
    }
    // This album has a line break (<br />) in the url. Test it to make sure ripme can handle these invalid urls
    @Test
    public void testAlbumWithInvalidChars() throws IOException {
        HentaiCafeRipper ripper = new HentaiCafeRipper(new URL("https://hentai.cafe/chobipero-club/"));
        testRipper(ripper);

    }
}
