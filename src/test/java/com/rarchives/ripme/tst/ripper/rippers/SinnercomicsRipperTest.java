package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.SinnercomicsRipper;

public class SinnercomicsRipperTest extends RippersTest {
    public void testSinnercomicsAlbum() throws IOException {
        SinnercomicsRipper ripper;

        // Comic test
        ripper = new SinnercomicsRipper(new URL("https://sinnercomics.com/comic/beyond-the-hotel-page-01/"));
        testRipper(ripper);

        // Homepage test
        ripper = new SinnercomicsRipper(new URL("https://sinnercomics.com/page/2/"));
        testRipper(ripper);

        // Pinup test
        ripper = new SinnercomicsRipper(new URL("https://sinnercomics.com/elsa-frozen-2/#comments"));
        testRipper(ripper);

    }

    public void testGetGID() throws IOException {
        URL url;
        SinnercomicsRipper ripper;

        // Comic test
        url = new URL("https://sinnercomics.com/comic/beyond-the-hotel-page-01/");
        ripper = new SinnercomicsRipper(url);
        assertEquals("beyond-the-hotel", ripper.getGID(url));

        // Homepage test
        url = new URL("https://sinnercomics.com/page/2/");
        ripper = new SinnercomicsRipper(url);
        assertEquals("homepage", ripper.getGID(url));

        // Comic test
        url = new URL("https://sinnercomics.com/elza-frozen-2/#comments");
        ripper = new SinnercomicsRipper(url);
        assertEquals("elza-frozen-2", ripper.getGID(url));
    }
}