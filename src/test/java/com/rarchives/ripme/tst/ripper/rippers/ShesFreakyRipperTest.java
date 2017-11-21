package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ShesFreakyRipper;

public class ShesFreakyRipperTest extends RippersTest {
    public void testShesFreakyRip() throws IOException {
        ShesFreakyRipper ripper = new ShesFreakyRipper(new URL("http://www.shesfreaky.com/gallery/nicee-snow-bunny-579NbPjUcYa.html"));
        testRipper(ripper);
    }
}
