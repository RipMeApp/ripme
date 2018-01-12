package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.XhamsterRipper;

public class XhamsterRipperTest extends RippersTest {

    public void testXhamsterAlbum1() throws IOException {
        XhamsterRipper ripper = new XhamsterRipper(new URL("https://xhamster.com/photos/gallery/sexy-preggo-girls-9026608"));
        testRipper(ripper);
    }

    public void testXhamsterAlbum2() throws IOException {
        XhamsterRipper ripper = new XhamsterRipper(new URL("https://xhamster.com/photos/gallery/japanese-dolls-4-asahi-mizuno-7254664"));
        testRipper(ripper);
    }
}
