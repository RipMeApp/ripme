package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.HentaiCafeRipper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class HentaicafeRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    @Disabled("20/05/2021 This test was disabled as the site has experienced notable downtime")
    public void testHentaiCafeAlbum() throws IOException, URISyntaxException {
        HentaiCafeRipper ripper = new HentaiCafeRipper(new URI("https://hentai.cafe/kikuta-the-oni-in-the-room/").toURL());
        testRipper(ripper);
    }
    // This album has a line break (<br />) in the url. Test it to make sure ripme can handle these invalid urls
    @Test
    @Tag("flaky")
    @Disabled("20/05/2021 This test was disabled as the site has experienced notable downtime")
    public void testAlbumWithInvalidChars() throws IOException, URISyntaxException {
        HentaiCafeRipper ripper = new HentaiCafeRipper(new URI("https://hentai.cafe/chobipero-club/").toURL());
        testRipper(ripper);

    }
}
