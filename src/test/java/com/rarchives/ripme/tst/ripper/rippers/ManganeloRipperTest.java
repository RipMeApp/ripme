package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ManganeloRipper;

public class ManganeloRipperTest extends RippersTest {
    public void testRip() throws IOException {
        ManganeloRipper ripper = new ManganeloRipper(new URL("https://manganelo.com/manga/demonic_housekeeper"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://manganelo.com/manga/demonic_housekeeper");
        ManganeloRipper ripper = new ManganeloRipper(url);
        assertEquals("demonic_housekeeper", ripper.getGID(url));
    }
}
