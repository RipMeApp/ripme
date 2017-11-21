package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.PichunterRipper;

public class PichunterRipperTest extends RippersTest {
    public void testPichunterModelPageRip() throws IOException {
        // A non-photoset
        PichunterRipper ripper = new PichunterRipper(new URL("https://www.pichunter.com/models/Madison_Ivy"));
        testRipper(ripper);
    }

    public void testPichunterGalleryRip() throws IOException {
        // a photo set
        PichunterRipper ripper = new PichunterRipper(new URL("http://www.pichunter.com/gallery/3270642/Its_not_only_those_who"));
        testRipper(ripper);
    }
}
