package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.SinnercomicsRipper;
import org.junit.jupiter.api.Test;

public class SinnercomicsRipperTest extends RippersTest {
    @Test
    public void testSinnercomicsAlbum() throws IOException {
        SinnercomicsRipper ripper;

        ripper = new SinnercomicsRipper(new URL("https://sinnercomics.com/comic/gw-addendum-page-01/"));
        testRipper(ripper);

    }

    public void testGetGID() throws IOException {
        URL url;
        SinnercomicsRipper ripper;

        // Comic test
        url = new URL("https://sinnercomics.com/comic/beyond-the-hotel-page-01/");
        ripper = new SinnercomicsRipper(url);
        assertEquals("beyond-the-hotel", ripper.getGID(url));

        // Comic test
        url = new URL("https://sinnercomics.com/elza-frozen-2/#comments");
        ripper = new SinnercomicsRipper(url);
        assertEquals("elza-frozen-2", ripper.getGID(url));
    }
}