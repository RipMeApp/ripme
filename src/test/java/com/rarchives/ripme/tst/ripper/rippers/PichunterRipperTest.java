package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.PichunterRipper;

public class PichunterRipperTest extends RippersTest {

    //    This test was commented out at 6/08/2018 because it was randomly failing due to issues with the site
    // see https://github.com/RipMeApp/ripme/issues/867
//    public void testPichunterModelPageRip() throws IOException {
//        // A non-photoset
//        PichunterRipper ripper = new PichunterRipper(new URL("https://www.pichunter.com/models/Madison_Ivy"));
//        testRipper(ripper);
//    }

    public void testPichunterGalleryRip() throws IOException {
        // a photo set
        PichunterRipper ripper = new PichunterRipper(new URL("http://www.pichunter.com/gallery/3270642/Its_not_only_those_who"));
        testRipper(ripper);
    }
}
