package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.SinnercomicsRipper;

public class SinnercomicsRipperTest extends RippersTest {
    public void testSinnercomicsAlbum() throws IOException {
        SinnercomicsRipper ripper = new SinnercomicsRipper(new URL("https://sinnercomics.com/comic/beyond-the-hotel-page-01/"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://sinnercomics.com/comic/beyond-the-hotel-page-01/");
        SinnercomicsRipper ripper = new SinnercomicsRipper(url);
        assertEquals("beyond-the-hotel", ripper.getGID(url));
    }
}